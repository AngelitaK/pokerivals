package com.smu.csd.pokerivals.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.pokemon.PokemonService;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.security.AuthenticationController;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // remove me and u will use the real database
@Slf4j
public class PokemonIntegrationTest {

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
    private PokemonService pokemonService;

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

        // Act
        uri = new URI(baseUrl + port + "/me");
        result = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), AuthenticationController.WhoAmI.class);

        assertEquals(200,result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("PLAYER",result.getBody().role());
    }

    @Test
    public void getPokemons_success() throws URISyntaxException {
        long totalNoPokemon = 0;
        int page=0;
        int limit=10;

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPath("/pokemon")
                .addParameter("page", String.valueOf(page++))
                .addParameter("limit", String.valueOf(limit));

        URI uri = builder.build();

        ResponseEntity<PokemonService.PokemonPageDTO> result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , PokemonService.PokemonPageDTO.class);
        totalNoPokemon = result.getBody().count();
        Set<Integer> pokemonIds = new HashSet<>(result.getBody().pokemons().stream().mapToInt(Pokemon::getId).boxed().toList());
        assertEquals(200, result.getStatusCode().value());

        log.info(result.getBody().pokemons().stream().mapToInt(Pokemon::getId).boxed().toList().toString());

        while (pokemonIds.size() < totalNoPokemon){
            builder.clearParameters()
                    .addParameter("page", String.valueOf(page++))
                    .addParameter("limit", String.valueOf(limit));

            uri = builder.build();

            result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , PokemonService.PokemonPageDTO.class);

            assertEquals(200, result.getStatusCode().value());
            pokemonIds.addAll(result.getBody().pokemons().stream().mapToInt(Pokemon::getId).boxed().toList());

            log.info(result.getBody().pokemons().stream().mapToInt(Pokemon::getId).boxed().toList().toString());

        }
        assertEquals(totalNoPokemon,pokemonIds.size());
    }

    @Test
    public void getPokemon_success() throws URISyntaxException {
        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPath("/pokemon/1");

        URI uri = builder.build();

        ResponseEntity<Pokemon> result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , Pokemon.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1,result.getBody().getId());
    }

    @Test
    public void getPokemonNature_success(){
        log.info(pokemonService.getPokemonNatures().toString());
    }
}
