package com.smu.csd.pokerivals.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.betting.service.DepositService;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled
public class StripeTest {
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

    private String username;

    @Autowired
    private PlayerRepository playerRepository;


    @BeforeEach
    public void loginPlayer() throws Exception {
        // Arrange
        String subOfPlayer = "def";
        when(verifier.verify(tokenMap.get(subOfPlayer))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(subOfPlayer);

        URI uri = new URI(baseUrl + port + "/auth/login");
        AuthenticationController.LoginDetails loginDetails1 = new AuthenticationController.LoginDetails(tokenMap.get(subOfPlayer));

        // Act
        ResponseEntity<AuthenticationController.WhoAmI> result = restTemplate.postForEntity(uri, loginDetails1, AuthenticationController.WhoAmI.class);
        username = storeCookie(result);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("PLAYER",result.getBody().role());

        verify(verifier).verify(tokenMap.get(subOfPlayer));
        verify(idTokenMock).getPayload();
    }

    @Test
    public void getPaymentLink_success() throws URISyntaxException {
        Player player = playerRepository.findById(username).orElseThrow();
        player.setEmail("jsumarlin.2022@scis.smu.edu.sg");
        player = playerRepository.save(player);

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setScheme("http")
                .setPort(port)
                .setPathSegments("deposit","start", "embedded");

        var resultEmbedded = restTemplate.exchange(builder.build(), HttpMethod.POST, createStatefulResponse(username), DepositService.EmbeddedPaymentDTO.class);
        assertEquals(200, resultEmbedded.getStatusCode().value());
        assertNotNull(resultEmbedded.getBody().clientSecret());

        builder
                .setPathSegments("deposit","start", "hosted");

        var resultHosted = restTemplate.exchange(builder.build(), HttpMethod.POST, createStatefulResponse(username), DepositService.HostedPaymentDTO.class);
        assertEquals(200, resultHosted.getStatusCode().value());
        assertNotNull(resultHosted.getBody().link());

        log.info(resultHosted.getBody().link());
    }
}
