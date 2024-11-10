package com.smu.csd.pokerivals.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.pokemon.entity.PokemonNature;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.tournament.service.TournamentAdminService;
import com.smu.csd.pokerivals.tournament.controller.TournamentAdminController;
import com.smu.csd.pokerivals.tournament.dto.TournamentPageDTO;
import com.smu.csd.pokerivals.tournament.entity.*;
import com.smu.csd.pokerivals.tournament.repository.TeamRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import com.smu.csd.pokerivals.user.repository.UserRepository;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test") // remove me and u will use the real database
@Slf4j
public class TournamentAdminIntegrationTest {

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
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private TeamRepository teamRepository;

    @MockBean
    private DateFactory dateFactory;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    public void loginAdmin() throws Exception {
        // Arrange
        String subOfPlayer = "abc";
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
    public void createAndModifyOpenTournament_success() throws URISyntaxException, JsonProcessingException {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        when(dateFactory.getToday()).thenReturn(ZonedDateTime.now());

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("admin","tournament");


        // 1: during registration period
        URI uri = builder.build();
        Tournament tournament = new OpenTournament("tournament", null,
                null, // will be converted by jackson mapper!
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().plusHours(0),
                        ZonedDateTime.now().plusHours(1)
                ));

        var result = restTemplate.exchange(uri,HttpMethod.POST,createStatefulResponse(username,tournament) , TournamentAdminController.TournamentIDMessage.class);

        assertEquals(200, result.getStatusCode().value());
        Tournament t = tournamentRepository.getTournamentById(result.getBody().tournamentId()).get();
        assertNotNull(t);

        log.info(t.getRegistrationPeriod().getRegistrationBegin().toString());
        builder.setPathSegments("admin","tournament",t.getId().toString());

        //modify during registration
        uri = builder.build();
        var dto = new TournamentAdminService.ModifyTournamentDTO(
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().plusHours(2),
                        ZonedDateTime.now().plusHours(2).plusMinutes(30)
                ),"changed_tournament", null,
                40,
                new Tournament.EstimatedTournamentPeriod(
                        ZonedDateTime.now().plusHours(3),
                        ZonedDateTime.now().plusHours(3).plusMinutes(30)
                )); // will be converted by jackson mapper!

        var result2 = restTemplate.exchange(uri,HttpMethod.PATCH,createStatefulResponse(username, dto) , Message.class);
        assertEquals(200, result2.getStatusCode().value());
        t = tournamentRepository.getTournamentById(result.getBody().tournamentId()).get();
        assertNotNull(t);
        assertEquals("changed_tournament",t.getName());

        builder.setPathSegments("admin","tournament","me").setParameter("page", "1").setParameter("limit", "10");
        uri = builder.build();

        var result3 = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , TournamentPageDTO.class);
        assertEquals(200, result3.getStatusCode().value());
        assertEquals(tournamentRepository.countTournamentByAdminUsername("fake_admin"),result3.getBody().count());


        //modify before registration
        builder.setPathSegments("admin","tournament",t.getId().toString());
        result2 = restTemplate.exchange(builder.build(),HttpMethod.PATCH,createStatefulResponse(username, dto) , Message.class);
        assertEquals(200, result2.getStatusCode().value());
    }

    @Test
    public void createAndModifyOpenTournament_fail_afterClosed() throws URISyntaxException, JsonProcessingException {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        when(dateFactory.getToday()).thenReturn(ZonedDateTime.now());
        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("admin","tournament");

        URI uri = builder.build();
        Tournament tournament = new OpenTournament("fail_tournament", null,
                null, // will be converted by jackson mapper!
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().minusHours(4),
                        ZonedDateTime.now().minusHours(3)
                ));

        var result = restTemplate.exchange(uri,HttpMethod.POST,createStatefulResponse(username,tournament) , TournamentAdminController.TournamentIDMessage.class);

        assertEquals(200, result.getStatusCode().value());
        Tournament t = tournamentRepository.getTournamentById(result.getBody().tournamentId()).get();
        assertNotNull(t);

        log.info(t.getRegistrationPeriod().getRegistrationBegin().toString());

        builder.setPathSegments("admin","tournament",t.getId().toString());

        uri = builder.build();
        var dto = new TournamentAdminService.ModifyTournamentDTO(
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().plusHours(2),
                        ZonedDateTime.now().plusHours(2).plusMinutes(30)
                ),"changed_tournament", null,
                40,
                new Tournament.EstimatedTournamentPeriod(
                        ZonedDateTime.now().plusHours(3),
                        ZonedDateTime.now().plusHours(3).plusMinutes(30)
                )); // will be converted by jackson mapper!

        var result2 = restTemplate.exchange(uri,HttpMethod.PATCH,createStatefulResponse(username, dto) , Message.class);
        assertEquals(400, result2.getStatusCode().value());
        t = tournamentRepository.getTournamentById(result.getBody().tournamentId()).get();
        assertNotNull(t);
        assertEquals("fail_tournament",t.getName());

    }

    @Test
    public void createAndInviteClosedTournament_success() throws URISyntaxException, JsonProcessingException {
        when(dateFactory.getToday()).thenReturn(ZonedDateTime.now());
        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("admin","tournament");

        URI uri = builder.build();
        Tournament tournament = new ClosedTournament("closed_tournament", null,
                null, // will be converted by jackson mapper!
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().plusHours(1),
                        ZonedDateTime.now().plusHours(2)
                ));

        var result = restTemplate.exchange(uri,HttpMethod.POST,createStatefulResponse(username,tournament) , TournamentAdminController.TournamentIDMessage.class);

        assertEquals(200, result.getStatusCode().value());
        Tournament t = tournamentRepository.getTournamentById(result.getBody().tournamentId()).get();
        assertNotNull(t);

        log.info(t.getRegistrationPeriod().getRegistrationBegin().toString());

        String[] playersToInvite = new String[]{
                "Mary Woodard","Samantha Brooks","Gerald Marsh"
        };

        var dto = new TournamentAdminController.InviteDTO(List.of(playersToInvite));

        builder.setPathSegments("admin", "tournament", "closed", t.getId().toString(),"invitation");

        uri = builder.build();
        var result2 = restTemplate.exchange(uri,HttpMethod.POST,createStatefulResponse(username,dto) , Message.class);

        assertTrue(result2.getStatusCode().is2xxSuccessful());

        builder.setPathSegments("admin","tournament","me").setParameter("page", "1").setParameter("limit", "10");
        uri = builder.build();

        var result3 = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , String.class);

        assertEquals(200, result3.getStatusCode().value());

        log.info(result3.getBody());

    }

    @Test
    public void getAllTeams_success() throws JsonProcessingException, URISyntaxException {
        when(dateFactory.getToday()).thenReturn(ZonedDateTime.now());

        Tournament tournament= tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(0,10000),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusHours(1)
                )
        ));
        log.info("created tournament");

        for (Player player: playerRepository.findAll()) {

            Team team = new Team(player, tournament);


            try {
                tournament.addTeam(team, ZonedDateTime.now());
            } catch (IllegalArgumentException e) {
                break;
            }
            Set<Integer> pokemonsToJoinWith = new HashSet<>();
            long totalPokemons = pokemonRepository.count();
            while (pokemonsToJoinWith.size() < 5){
                pokemonsToJoinWith.add(random.nextInt(1,(int)totalPokemons +1));
            }


            log.info(pokemonsToJoinWith.toString());

            for (int pokemonId : pokemonsToJoinWith) {
                Pokemon pokemon = pokemonRepository.findById((int) pokemonId).orElseThrow();

                ChosenPokemon chosenPokemon = new ChosenPokemon(team, pokemon);
                chosenPokemon.setAbility(getRandomSetElement(pokemon.getAbilities()));

                List<String> moveNames = new ArrayList<>();


                pokemon.getMoves().forEach(move ->{
                    moveNames.add(move.getName());
                });
                Collections.shuffle(moveNames);
                List<String> chosenMoves = null;
                try {
                    chosenMoves = moveNames.subList(0,4);
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                chosenMoves.forEach(moveName ->{
                    chosenPokemon.learnMove(moveRepository.findById(moveName).orElseThrow());
                });


                chosenPokemon.setNature(randomEnum(PokemonNature.class));
                team.addChosenPokemon(chosenPokemon);
            }
            // join

            team = teamRepository.save(team);
        }

        log.info("Done with adding teams");

        long totalNoMatches = 0;
        int page=0;
        int limit=10;

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("admin", "tournament", tournament.getId().toString(), "team")
                .addParameter("page", String.valueOf(page++))
                .addParameter("limit", String.valueOf(limit));

        URI uri = builder.build();

        var result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , TournamentAdminService.TeamPageDTO.class);
        totalNoMatches = result.getBody().count();
        var noPlayersCaptured = result.getBody().teams().size();
        assertEquals(200, result.getStatusCode().value());

        while (noPlayersCaptured < totalNoMatches){
            builder.clearParameters()
                    .addParameter("page", String.valueOf(page++))
                    .addParameter("limit", String.valueOf(limit));

            uri = builder.build();

            result = restTemplate.exchange(uri,HttpMethod.GET,createStatefulResponse(username) , TournamentAdminService.TeamPageDTO.class);

            assertEquals(200, result.getStatusCode().value());
            noPlayersCaptured += result.getBody().teams().size();
        }
        assertEquals(totalNoMatches, noPlayersCaptured);


        tournamentRepository.delete(tournament);

    }


    @Test
    public void deleteTeams_success() throws JsonProcessingException, URISyntaxException {
        when(dateFactory.getToday()).thenReturn(ZonedDateTime.now());

        Tournament tournament= tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(0,10000),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusHours(1)
                )
        ));
        log.info("created tournament");

        Set<String> usernamesJoinedTournament = new HashSet<>();

        for (Player player: playerRepository.findAll()) {

            Team team = new Team(player, tournament);


            try {
                tournament.addTeam(team, ZonedDateTime.now());
            } catch (IllegalArgumentException e) {
                break;
            }

            usernamesJoinedTournament.add(player.getUsername());
            Set<Integer> pokemonsToJoinWith = new HashSet<>();
            long count = pokemonRepository.count();
            while (pokemonsToJoinWith.size() < 5){
                pokemonsToJoinWith.add(random.nextInt(1,(int) count +1));
            }


            log.info(pokemonsToJoinWith.toString());

            for (int pokemonId : pokemonsToJoinWith) {
                Pokemon pokemon = pokemonRepository.findById((int) pokemonId).orElseThrow();

                ChosenPokemon chosenPokemon = new ChosenPokemon(team, pokemon);
                chosenPokemon.setAbility(getRandomSetElement(pokemon.getAbilities()));

                List<String> moveNames = new ArrayList<>();


                pokemon.getMoves().forEach(move ->{
                    moveNames.add(move.getName());
                });
                Collections.shuffle(moveNames);
                List<String> chosenMoves = null;
                try {
                    chosenMoves = moveNames.subList(0,4);
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }

                chosenMoves.forEach(moveName ->{
                    chosenPokemon.learnMove(moveRepository.findById(moveName).orElseThrow());
                });

                chosenPokemon.setNature(randomEnum(PokemonNature.class));
                team.addChosenPokemon(chosenPokemon);
            }
            // join

            team = teamRepository.save(team);
        }

        log.info("Done with adding teams");

        int numberOfPlayersRemaining = usernamesJoinedTournament.size();

        URIBuilder builder = new URIBuilder()
                .setHost("localhost")
                .setPort(port)
                .setScheme("http")
                .setPathSegments("admin", "tournament", tournament.getId().toString(), "team", "player", "fake_player");

        URI uri = builder.build();

        var result2 = restTemplate.exchange(uri, HttpMethod.DELETE, createStatefulResponse(username), Message.class);
        assertEquals(400, result2.getStatusCode().value());

        when(dateFactory.getToday()).thenReturn(ZonedDateTime.now().plusHours(2));

        int noDeleted = 0;
        for(String playerUsername : usernamesJoinedTournament) {

            builder
                    .setHost("localhost")
                    .setPort(port)
                    .setScheme("http")
                    .setPathSegments("admin", "tournament", tournament.getId().toString(), "team", "player", playerUsername);

            uri = builder.build();

            result2 = restTemplate.exchange(uri, HttpMethod.DELETE, createStatefulResponse(username), Message.class);
            assertEquals(200, result2.getStatusCode().value());
            noDeleted++;

            assertEquals(usernamesJoinedTournament.size()-noDeleted,teamRepository.countByTournamentIdAndAdminUsername(tournament.getId(),username));

            long totalNoMatches = usernamesJoinedTournament.size()-noDeleted;
            int page = 0;
            int limit = 10;
            var noPlayersCaptured = 0;


            while (noPlayersCaptured < totalNoMatches) {
                log.info("page {}",page);
                builder.clearParameters()
                        .setPathSegments("admin", "tournament", tournament.getId().toString(), "team")
                        .addParameter("page", String.valueOf(page++))
                        .addParameter("limit", String.valueOf(limit));

                uri = builder.build();

                var result = restTemplate.exchange(uri, HttpMethod.GET, createStatefulResponse(username), TournamentAdminService.TeamPageDTO.class);

                totalNoMatches = result.getBody().count();
                assertEquals(200, result.getStatusCode().value());
                log.info(String.valueOf(result.getBody().teams().size()));
                noPlayersCaptured += result.getBody().teams().size();
            }
            assertEquals(totalNoMatches, noPlayersCaptured);
            assertEquals(--numberOfPlayersRemaining,noPlayersCaptured);
            log.info("Expected total {}",numberOfPlayersRemaining);
        }

        tournamentRepository.deleteById(tournament.getId());

    }

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    private <E> E getRandomSetElement(Set<E> set) {
        var list = new ArrayList<>(set);
        return list.get(random.nextInt(0,list.size()));
    }


}
