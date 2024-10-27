package com.smu.csd.pokerivals.security;

import com.smu.csd.pokerivals.user.entity.User;
import com.smu.csd.pokerivals.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.security.authentication.CompletedGoogleAuthentication;
import com.smu.csd.pokerivals.security.authentication.IncompleteGoogleAuthentication;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * {@link AuthenticationProvider} that uses Google JWT string
 */
@Component
@Slf4j
public class GoogleAuthenticationProvider implements AuthenticationProvider {

    private UserRepository userRepository;

    private GoogleIdTokenVerifier verifier;

    @Autowired
    public GoogleAuthenticationProvider(UserRepository userRepository, GoogleIdTokenVerifier verifier) {
        this.userRepository = userRepository;
        this.verifier = verifier;
    }

    /**
     * Converts {@link IncompleteGoogleAuthentication} to {@link CompletedGoogleAuthentication}
     * @param authentication must be of the {@link IncompleteGoogleAuthentication} type
     * @return  {@link CompletedGoogleAuthentication}
     *
     * @throws IllegalArgumentException authentication argument not of the correct type
     * @throws BadCredentialsException token is expired/invalid
     * @throws java.util.NoSuchElementException user with the Google Subject Identifier not found
     */
    @SneakyThrows
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (authentication instanceof IncompleteGoogleAuthentication token){

            // (Receive idTokenString by HTTPS POST)
                GoogleIdToken idToken = verifier.verify((String) token.getCredentials());
                if (idToken != null) {
                    Payload payload = idToken.getPayload();
    
                    // Print user identifier
                    String userId = payload.getSubject();
    
                    User user = userRepository.findOneByGoogleSub(userId).orElseThrow();

                    return new CompletedGoogleAuthentication(user);
                } else {
                    log.info("Expired token");
                    throw new BadCredentialsException("Expired token");
                }

        } else {
            throw new IllegalArgumentException("Wrong authentication token"){};
        }
    }

    /**
     * Check whether the authentication is supported
     * @param authentication checking this class
     * @return whether the authentication is supported
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(IncompleteGoogleAuthentication.class);
    }
}