package com.smu.csd.pokerivals.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.exception.MacInvalidException;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.repository.AdminRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.MacAlgorithmSpec;
import software.amazon.awssdk.services.kms.model.VerifyMacResponse;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class AdminService {
    private final AdminRepository adminRepo;
    private final KmsClient kmsClient;
    private final LambdaAsyncClient lambdaClient;
    private final GoogleIdTokenVerifier verifier;

    @Value("${email.link-account.validity-seconds}")
    private Integer validitySeconds;

    @Value("${email.link-account.kms-key-id}")
    private String kmsKeyId;

    @Value("${email.link-account.lambda-ARN}")
    private String lambdaArn;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
     public AdminService(AdminRepository repo, KmsClient kmsClient, LambdaAsyncClient lambdaClient, GoogleIdTokenVerifier verifier)  {
         this.adminRepo = repo;
         this.kmsClient = kmsClient;
         this.lambdaClient = lambdaClient;
         this.verifier = verifier;
     }

    /**
     * Register a new Admin object into the system AND send and email (refer to {@link AdminService#sendLinkEmail(String)}
     *
     * @param inviterUsername username of the admin inviting new admin
     * @param admin new admin
     * @throws JsonProcessingException issue with {@link ObjectMapper#writeValueAsBytes(Object)}
     * @throws ExecutionException issue with {@link Future#get()}
     * @throws InterruptedException issue with {@link Future#get()}
     */
     @RolesAllowed("ROLE_ADMIN")
     @Transactional
     public void register(String inviterUsername, Admin admin) throws JsonProcessingException, ExecutionException, InterruptedException{
         admin = adminRepo.save(admin);

         Admin inviter = adminRepo.findById(inviterUsername).orElseThrow();
         inviter.addInvitee(admin);
         adminRepo.save(inviter);

         sendLinkEmail(admin.getUsername());
     }

     private record LambdaEmailDTO(String username, String email){};

    /**
     * Send Link Email to the email of the admin with given username
     * See LinkEmail Lambda Function
     * <p>
     * Uses email.link-account.validity-seconds, email.link-account.kms-key-id, email.link-account.lambda-ARN
     *
     * @param username username of admin to send email to
     * @throws JsonProcessingException issue with {@link ObjectMapper#writeValueAsBytes(Object)}
     * @throws ExecutionException issue with {@link Future#get()}
     * @throws InterruptedException issue with {@link Future#get()}
     */
     @RolesAllowed("ROLE_ADMIN")
     public void sendLinkEmail(String username) throws JsonProcessingException, ExecutionException, InterruptedException {
        Admin admin = adminRepo.findById(username).orElseThrow(()-> new NoSuchElementException("Admin does not exist!"));

         byte[] json = jacksonObjectMapper.writeValueAsBytes(new LambdaEmailDTO(admin.getUsername(), admin.getEmail()));

         CompletableFuture<InvokeResponse> future = lambdaClient.invoke(b -> {
             b.functionName(lambdaArn)
                     .invocationType(InvocationType.EVENT)
                     .payload(SdkBytes.fromByteArray(
                                json
                             ));
         });

         future.get();
     }

    @RolesAllowed("ROLE_ADMIN")
    public List<Admin> getInvitees(String adminUsername) {
        return adminRepo.findAdminsInvitedBy(adminUsername);
    }

    /**
     * DTO containing data obtained from an attempt to link Google Account to an Admin
     */
    @AllArgsConstructor
    @NoArgsConstructor
     @Getter
     public static class LinkAccountDTO {
        @Getter
        private String username;
        private String email;

        private long time;
        private String mac;

        @Getter
        private String credentials;

        /**
         * Checks whether the data contained is valid
         * @param validitySeconds how long the email should have been valid for
         * @return not expired [true] OR expired [false]
         */
        public boolean checkValidity(long validitySeconds){
            return ((System.currentTimeMillis() / 1000L) - time) < validitySeconds;
        }

        /**
         * Check whether Mac is valid
         * Must use the same KMS Key as the one used in lambda
         * @param client {@link KmsClient} used
         * @param keyId ID of HMAC Key
         * @return Mac valid or not
         */
        public boolean checkMac(KmsClient client, String keyId){
            VerifyMacResponse verifyMacResponse = client.verifyMac( r -> {
                r.keyId(keyId)
                        .mac(SdkBytes.fromByteArray(
                                Base64.getUrlDecoder().decode(mac)
                        ))
                        .macAlgorithm(MacAlgorithmSpec.HMAC_SHA_512)
                        .message(SdkBytes.fromByteArray(
                                (username + email + Long.toString(time)).getBytes()
                        ));
            });

            return verifyMacResponse.macValid();
        }

        /**
         * Get when the email was sent
         * @return when the email was sent
         */
         public Date getTime(){
            return new Date(time * 1000L);
         }

     }

    /**
     * Link Admin to a Google Account
     *
     * @param dto data obtained from the link email
     * @exception BadCredentialsException whether the token was valid
     */
     @SneakyThrows
     public void linkEmail(LinkAccountDTO dto){
        if (! (dto.checkValidity(validitySeconds) && dto.checkMac(kmsClient, kmsKeyId) )){
            throw new MacInvalidException();
        }

         Admin admin = adminRepo.findById(dto.getUsername()).orElseThrow(()-> new NoSuchElementException("Admin does not exist!"));


             GoogleIdToken idToken = verifier.verify( dto.getCredentials());
             if (idToken != null) {
                 GoogleIdToken.Payload payload = idToken.getPayload();

                 String userId = payload.getSubject();
                 admin.updateGoogleSub(dto.getTime(),userId);
                 admin.setActiveSince(Date.from(Instant.now()));
                 adminRepo.save(admin);

             } else {
                 throw new BadCredentialsException("Expired token");
             }
     }
}
