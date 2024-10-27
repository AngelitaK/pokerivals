package com.smu.csd.pokerivals.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.user.controller.PlayerController;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.VerifyMacResponse;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // remove me and u will use the real database
@Slf4j
public class SpringBootIntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;




    // Mocked for google
    @MockBean
    private GoogleIdTokenVerifier verifier;

    @Mock
    private GoogleIdToken.Payload payload;

    @Mock
    private  GoogleIdToken idTokenMock;

    // Mocked for AWS
    @MockBean
    private KmsClient kmsClient;

    @MockBean
    private LambdaAsyncClient lambdaAsyncClient;

    // object mapper because sometimes the serialisation behaves weird on time field
    private final ObjectMapper objectMapper = new ObjectMapper();


    // This authentication depends on a third party, thus we used mocking
    @Test
    public void registerPlayer_success() throws Exception{

        String subOfNewPlayer = "ghi";
        when(verifier.verify(tokenMap.get(subOfNewPlayer))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(subOfNewPlayer);

        PlayerController.PlayerRegistrationDTO playerDTO = new PlayerController.PlayerRegistrationDTO(tokenMap.get("ghi"), new Player(
                "not_important",subOfNewPlayer
        ));

        URI uri = new URI(baseUrl + port + "/player");

        ResponseEntity<Message> result = restTemplate.postForEntity(uri, playerDTO, Message.class);

        assertNotNull(result);
        log.info(String.valueOf(result.getBody()));
        assertEquals(200, result.getStatusCode().value());

        uri = new URI(baseUrl + port + "/auth/login");
        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(subOfNewPlayer));

        // Act
        ResponseEntity<AuthenticationController.WhoAmI> result2 = restTemplate.postForEntity(uri, loginDetails1, AuthenticationController.WhoAmI.class);

        // Assert
        assertEquals(200, result2.getStatusCode().value());
        assertNotNull(result2.getBody().role());
        assertEquals("PLAYER", result2.getBody().role());
    }

    @Test
    public void loginAdmin_Success() throws Exception {
        // Arrange
        String subOfPlayer = "abc";
        when(verifier.verify(tokenMap.get(subOfPlayer))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(subOfPlayer);

        URI uri = new URI(baseUrl + port + "/auth/login");
        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(subOfPlayer));

        // Act
        ResponseEntity<AuthenticationController.WhoAmI> result = restTemplate.postForEntity(uri, loginDetails1, AuthenticationController.WhoAmI.class);
        String username = storeCookie(result);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("ADMIN",result.getBody().role());

        verify(verifier).verify(tokenMap.get(subOfPlayer));
        verify(idTokenMock).getPayload();

        // Act
        uri = new URI(baseUrl + port + "/me");
        result = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), AuthenticationController.WhoAmI.class);

        assertEquals(200,result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("ADMIN",result.getBody().role());
    }

    @Test
    public void loginPlayer_Success() throws Exception {
        // Arrange
        String subOfPlayer = "def";
        when(verifier.verify(tokenMap.get(subOfPlayer))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(subOfPlayer);

        URI uri = new URI(baseUrl + port + "/auth/login");
        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(subOfPlayer));

        // Act
        ResponseEntity<AuthenticationController.WhoAmI> result = restTemplate.postForEntity(uri, loginDetails1, AuthenticationController.WhoAmI.class);
        String username = storeCookie(result);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("PLAYER",result.getBody().role());

        verify(verifier).verify(tokenMap.get(subOfPlayer));
        verify(idTokenMock).getPayload();

        // Act
        uri = new URI(baseUrl + port + "/me");
        result = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), AuthenticationController.WhoAmI.class);

        assertEquals(200,result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("PLAYER",result.getBody().role());

        log.info(String.valueOf(result.getBody()));
    }


    @Test
    public void login_failure_userNotFound() throws Exception {
        // Arrange

        URI uri = new URI(baseUrl + port + "/auth/login");

        GoogleIdToken idTokenMock = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock (GoogleIdToken.Payload.class);

        String subOfNonExisting = "mno";
        when(verifier.verify(tokenMap.get(subOfNonExisting))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(subOfNonExisting);

        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(subOfNonExisting));

        // Act
        ResponseEntity<String> result = restTemplate.postForEntity(uri, loginDetails1, String.class);

        // Assert
        assertEquals(404, result.getStatusCode().value());
        assertNotNull(result);
        verify(verifier).verify(tokenMap.get(subOfNonExisting));
        verify(idTokenMock).getPayload();

    }

    @Test
    public void login_failure_badToken() throws Exception {
        // Arrange

        URI uri = new URI(baseUrl + port + "/auth/login");

        GoogleIdToken idTokenMock = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock (GoogleIdToken.Payload.class);

        String subOfNonExisting = "mno";
        when(verifier.verify(tokenMap.get(subOfNonExisting))).thenReturn(null);

        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(subOfNonExisting));

        // Act
        ResponseEntity<String> result = restTemplate.postForEntity(uri, loginDetails1, String.class);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertNotNull(result);
        verify(verifier).verify(tokenMap.get(subOfNonExisting));
    }

    @Test
    public void logoutAdmin_Success() throws Exception {

        loginAdmin_Success();
        String username = "fake_admin";

        // Arrange
        URI uri = new URI(baseUrl + port + "/auth/logout");

        // Act
        ResponseEntity<Message> result =  restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), Message.class);

        // Assert
        assertEquals(200, result.getStatusCode().value());

        // Act
        uri = new URI(baseUrl + port + "/me");
        ResponseEntity<AuthenticationController.WhoAmI> result2 = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), AuthenticationController.WhoAmI.class);

        assertEquals(403,result2.getStatusCode().value());
    }

    @Test
    public void logoutPlayer_Success() throws Exception {

        loginPlayer_Success();
        String username = "fake_player";

        // Arrange
        URI uri = new URI(baseUrl + port + "/auth/logout");

        // Act
        ResponseEntity<Message> result =  restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), Message.class);

        // Assert
        assertEquals(200, result.getStatusCode().value());

        // Act
        uri = new URI(baseUrl + port + "/me");
        ResponseEntity<AuthenticationController.WhoAmI> result2 = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), AuthenticationController.WhoAmI.class);

        assertEquals(403,result2.getStatusCode().value());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void inviteAdmin() throws Exception {
        loginAdmin_Success();
        when(lambdaAsyncClient.invoke(any(Consumer.class))).thenReturn( CompletableFuture.completedFuture("not_important"));

        String username = "fake_admin";
        // Arrange
        URI uri = new URI(baseUrl + port + "/admin");

        Admin admin = new Admin("new_admin", "not_important");

        // Act
        ResponseEntity<Message> result = restTemplate.postForEntity(uri,createStatefulResponse(username,admin), Message.class);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        verify(lambdaAsyncClient).invoke(any(Consumer.class));

        // --------------------------------------------------------

        // Arrange
        long time = System.currentTimeMillis() / 1000L - 100L;;
        String mac = "mac";
        String sub = "jkl";

        when(verifier.verify(tokenMap.get(sub))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(sub);

        VerifyMacResponse macResponse = mock(VerifyMacResponse.class);

        when(kmsClient.verifyMac(any(Consumer.class))).thenReturn(macResponse);
        when(macResponse.macValid()).thenReturn(true);

        uri = new URI(baseUrl + port + "/admin/link");

        AdminService.LinkAccountDTO linkAccountDTO
                = new AdminService.LinkAccountDTO(admin.getUsername(),admin.getEmail(),time,mac,tokenMap.get(sub));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(linkAccountDTO), headers);

        // Act
        result = restTemplate.postForEntity(uri,request, Message.class);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        verify(kmsClient).verifyMac(any(Consumer.class));
        verify(verifier).verify(tokenMap.get(sub));

        // Try login
        reset(verifier);
        when(verifier.verify(tokenMap.get(sub))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(sub);

        uri = new URI(baseUrl + port + "/auth/login");
        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(sub));

        // Act
        ResponseEntity<AuthenticationController.WhoAmI> result2 = restTemplate.postForEntity(uri, loginDetails1, AuthenticationController.WhoAmI.class);

        // Assert
        assertEquals(200, result2.getStatusCode().value());
        assertNotNull(result2.getBody().role());
        assertEquals(result2.getBody().username(),"new_admin");
        assertEquals("ADMIN", result2.getBody().role());

        verify(verifier).verify(tokenMap.get(sub));
    }


}
