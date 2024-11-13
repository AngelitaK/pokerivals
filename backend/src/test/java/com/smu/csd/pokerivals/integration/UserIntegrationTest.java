package com.smu.csd.pokerivals.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.service.PlayerService;
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
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // remove me and u will use the real database
@Slf4j
public class UserIntegrationTest {

    @LocalServerPort
    private int port;

    private final String host = "localhost";
    private final String scheme = "http";

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

    @BeforeEach
    public void loginPlayer() throws Exception {
        // Arrange
        String subOfPlayer = "def";
        when(verifier.verify(tokenMap.get(subOfPlayer))).thenReturn(idTokenMock);
        when(idTokenMock.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn(subOfPlayer);

        URI uri = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPath("/auth/login")
                .build();
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
        uri = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPath("/me")
                .build();
        result = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), AuthenticationController.WhoAmI.class);

        assertEquals(200,result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("PLAYER",result.getBody().role());
    }

    static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }


    @Test
    public void getAllPlayers_success() throws URISyntaxException {
        getALlPlayers();
    }

    @Test
    public void makingAndUnmakingFriends_success() throws URISyntaxException {
        Set<String> set = getALlPlayers();

        int noOfNewFriendsToMake = 5;
        Set<String> newFriendsUsernames = new HashSet<>();
        while(noOfNewFriendsToMake >0){
            String newFriendUsername = getRandomSetElement(set);
            if (newFriendUsername == null || newFriendUsername.equals(username)){
                continue;
            }
            log.info("Making friends with {}", newFriendUsername);
            URIBuilder builder = new URIBuilder()
                    .setHost(host)
                    .setPort(port)
                    .setScheme(scheme)
                    .setPathSegments("player","me","friend",newFriendUsername);

            URI uri = builder.build();

            ResponseEntity<Message> result = restTemplate.exchange(uri,HttpMethod.POST,createStatefulResponse(username) , Message.class);

            assertEquals(200,result.getStatusCode().value());
            newFriendsUsernames.add(newFriendUsername);
            noOfNewFriendsToMake--;
        }

        long noNotMyFriends = countMyFriends(true);
        long noMyFriends = countMyFriends(false);

        log.info("No of people my friend {}, not my {}", noMyFriends,noNotMyFriends);

        assertEquals(newFriendsUsernames.size(), noMyFriends);
        assertEquals(set.size(), noMyFriends + noNotMyFriends);

        newFriendsUsernames.forEach(friendUsername ->{
            URIBuilder builder = new URIBuilder()
                    .setHost("localhost")
                    .setPort(port)
                    .setScheme("http")
                    .setPathSegments("player","me","friend",friendUsername);

            URI uri = null;
            try {
                uri = builder.build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            ResponseEntity<Message> result = restTemplate.exchange(uri,HttpMethod.DELETE,createStatefulResponse(username) , Message.class);

            assertEquals(200,result.getStatusCode().value());
        });

        assertEquals(set.size(), countMyFriends(true) + countMyFriends(false));
        assertEquals(0, countMyFriends(false));


    }


    public Set<String> getALlPlayers() throws URISyntaxException {
        long totalNoPlayer = 0;
        int page=0;
        int limit=10;

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPath("/player")
                .addParameter("page", String.valueOf(page++))
                .addParameter("limit", String.valueOf(limit))
                .addParameter("query", "");

        URI uri = builder.build();

        ResponseEntity<PlayerService.PlayerPageDTO> result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , PlayerService.PlayerPageDTO.class);
        totalNoPlayer = result.getBody().count();
        Set<String> playerUsernames = new HashSet<>(result.getBody().players().stream().map(Player::getId).toList());
        assertEquals(200, result.getStatusCode().value());

        log.info(result.getBody().players().stream().map(Player::getId).toList().toString());

        while (playerUsernames.size() < totalNoPlayer){
            builder.clearParameters()
                    .addParameter("page", String.valueOf(page++))
                    .addParameter("limit", String.valueOf(limit))
                    .addParameter("query", "");

            uri = builder.build();

            result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , PlayerService.PlayerPageDTO.class);

            assertEquals(200, result.getStatusCode().value());
            playerUsernames.addAll(result.getBody().players().stream().map(Player::getId).toList());

            log.info(result.getBody().players().stream().map(Player::getId).toList().toString());

        }
        assertEquals(totalNoPlayer, playerUsernames.size());

        return playerUsernames;
    }


    public String getPlayer(String username) throws URISyntaxException {
        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("player",username);

        URI uri = builder.build();

        ResponseEntity<String> result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , String.class);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().contains(username));

        return result.getBody();
    }

    public void getPlayer_success() throws URISyntaxException {
        getPlayer("fake_player");
    }

    @Test
    public void makeFriend_fail_withSelf(){
        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("player","me","friend",username);

        URI uri = null;
        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ResponseEntity<Message> result = restTemplate.exchange(uri,HttpMethod.POST,createStatefulResponse(username) , Message.class);

        assertEquals(400,result.getStatusCode().value());

        result = restTemplate.exchange(uri,HttpMethod.DELETE,createStatefulResponse(username) , Message.class);

        assertEquals(400,result.getStatusCode().value());
    }

    public long countMyFriends(boolean non_friend) throws URISyntaxException {
        long totalNoPlayer = 0;
        int page=0;
        int limit=10;

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPath(non_friend ? "/player/me/non-friend" :  "/player/me/friend")
                .addParameter("page", String.valueOf(page++))
                .addParameter("limit", String.valueOf(limit));

        URI uri = builder.build();

        ResponseEntity<PlayerService.PlayerPageDTO> result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , PlayerService.PlayerPageDTO.class);
        totalNoPlayer = result.getBody().count();
        Set<String> pokemonIds = new HashSet<>(result.getBody().players().stream().map(Player::getId).toList());
        assertEquals(200, result.getStatusCode().value());

        log.info(result.getBody().players().stream().map(Player::getId).toList().toString());

        while (pokemonIds.size() < totalNoPlayer){
            builder.clearParameters()
                    .addParameter("page", String.valueOf(page++))
                    .addParameter("limit", String.valueOf(limit));

            uri = builder.build();

            result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , PlayerService.PlayerPageDTO.class);

            assertEquals(200, result.getStatusCode().value());
            pokemonIds.addAll(result.getBody().players().stream().map(Player::getId).toList());

            log.info(result.getBody().players().stream().map(Player::getId).toList().toString());

        }
        assertEquals(totalNoPlayer,pokemonIds.size());

        return totalNoPlayer;
    }

    @Test
    public void testClan_success() throws URISyntaxException {
        var factory =  restTemplate.getRestTemplate().getRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());

        String clan = "magma";

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("player","me","clan",clan);

        URI uri = builder.build();

        ResponseEntity<Message> result = restTemplate.exchange(uri,HttpMethod.PATCH,createStatefulResponse(username) , Message.class);
        assertEquals(200,result.getStatusCode().value());

        String playerJson = getPlayer(username);
        assertTrue(playerJson.contains(clan));

        result = restTemplate.exchange(uri,HttpMethod.PATCH,createStatefulResponse(username) , Message.class);
        assertEquals(400,result.getStatusCode().value());

        builder.setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("player","me","clan","galactic");

        uri = builder.build();
        result = restTemplate.exchange(uri,HttpMethod.PATCH,createStatefulResponse(username) , Message.class);
        assertEquals(400,result.getStatusCode().value());

        builder.setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("player","me","clan","abc");

        uri = builder.build();
        result = restTemplate.exchange(uri,HttpMethod.PATCH,createStatefulResponse(username) , Message.class);
        assertEquals(404,result.getStatusCode().value());


        restTemplate.getRestTemplate().setRequestFactory(factory);
    }


}


