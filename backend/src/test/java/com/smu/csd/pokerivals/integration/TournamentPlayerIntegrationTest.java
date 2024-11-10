package com.smu.csd.pokerivals.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.POKEMON_NATURE;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.tournament.service.TournamentPlayerService;
import com.smu.csd.pokerivals.tournament.dto.TournamentPageDTO;
import com.smu.csd.pokerivals.tournament.entity.*;
import com.smu.csd.pokerivals.tournament.repository.ChosenPokemonRepository;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test") // remove me and u will use the real database
@Slf4j
public class TournamentPlayerIntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";
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
    private GoogleIdToken idTokenMock;

    private String username;

    private String adminUsername;

    private Tournament tournament;

    @Autowired
    private MoveRepository moveRepository;


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
        assertEquals("PLAYER", result.getBody().role());

        verify(verifier).verify(tokenMap.get(subOfPlayer));
        verify(idTokenMock).getPayload();

        reset(verifier,idTokenMock);
    }

    private int[] pokemonsToJoinWith = new int[]{
            2,4,6,3,7
    };

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ChosenPokemonRepository chosenPokemonRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void joinTournament_success() throws URISyntaxException, JsonProcessingException {
        URIBuilder builder = new URIBuilder().setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player","tournament", "me")
                .setParameter("page", "1")
                .setParameter("limit", "10");

        var result2 = restTemplate.exchange(builder.build(),HttpMethod.GET,createStatefulResponse(username), TournamentPageDTO.class);

        assertEquals(200, result2.getStatusCode().value());
        long initialTournaments = result2.getBody().count();

        long initialChosenPokemon =chosenPokemonRepository.count();

        tournament= tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(0,10000),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusHours(1)
                )
        ));
        log.info("created tournament {}", tournament.getId());


        log.info(tournament.getId().toString());
        Player player = playerRepository.findById(username).orElseThrow();

        TournamentPlayerService.JoinTournamentDTO dto = generateJoinDTO();

        log.info(new ObjectMapper().writeValueAsString(dto));

        builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player","tournament", tournament.getId().toString(), "join");

        var result = restTemplate.exchange(builder.build(),HttpMethod.POST,createStatefulResponse(username,dto), Message.class);

        assertEquals(200, result.getStatusCode().value());

        player = playerRepository.findById(username).orElseThrow();
        tournament = tournamentRepository.findById(tournament.getId()).orElseThrow();
        Team team = teamRepository.findById(new Team.TeamId(player,tournament)).orElseThrow();
        assertEquals("fake_player", team.getPlayer().getUsername());
        assertEquals("abc", team.getTournament().getName());

        assertEquals(pokemonsToJoinWith.length,chosenPokemonRepository.count()-initialChosenPokemon);
        for(int pokemonId : pokemonsToJoinWith) {
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();
            assertEquals(
                    new Team.TeamId(player, tournament),
                    chosenPokemonRepository.findById(new ChosenPokemon.PokemonId(team, pokemon)).orElseThrow().getPokemonId().getTeamId()
            );
        }

        builder
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player","tournament", "me")
                .setParameter("page", "1")
                .setParameter("limit", "10");

        var result3 = restTemplate.exchange(builder.build(),HttpMethod.GET,createStatefulResponse(username), TournamentPageDTO.class);

        assertEquals(200, result2.getStatusCode().value());
        assertEquals(1, result3.getBody().count()-initialTournaments);

    }

    @Test
    public void joinTournament_fail_tooEarly() throws URISyntaxException, JsonProcessingException {

        tournament = tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(0, 10000),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().plusMinutes(30),
                        ZonedDateTime.now().plusHours(1)
                )
        ));
        log.info("created tournament");

        log.info(tournament.getId().toString());
        Player player = playerRepository.findById(username).orElseThrow();

        TournamentPlayerService.JoinTournamentDTO dto = generateJoinDTO();

        URIBuilder builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player", "tournament", tournament.getId().toString(), "join");

        var result = restTemplate.exchange(builder.build(), HttpMethod.POST, createStatefulResponse(username, dto), Message.class);

        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    public void joinTournament_fail_tooLate() throws URISyntaxException, JsonProcessingException {

        tournament = tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(0, 10000),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().minusHours(4),
                        ZonedDateTime.now().minusHours(3)
                )
        ));
        log.info("created tournament");

        log.info(tournament.getId().toString());
        Player player = playerRepository.findById(username).orElseThrow();

        TournamentPlayerService.JoinTournamentDTO dto = generateJoinDTO();

        URIBuilder builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player", "tournament", tournament.getId().toString(), "join");

        var result = restTemplate.exchange(builder.build(), HttpMethod.POST, createStatefulResponse(username, dto), Message.class);

        assertEquals(400, result.getStatusCode().value());
    }

    private TournamentPlayerService.JoinTournamentDTO generateJoinDTO(){
        List<TournamentPlayerService.RawPokemonChoiceDTO> input = new ArrayList<>();

        for(int pokemonId : pokemonsToJoinWith){
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();

            String ability = getRandomSetElement(pokemon.getAbilities()).getName();

            Set<String> moveNames = new HashSet<>();
            while(moveNames.size() < 3){
                Move m = getRandomSetElement(pokemon.getMoves());
                moveNames.add(m.getName());
            }
            POKEMON_NATURE nature = randomEnum(POKEMON_NATURE.class);

            TournamentPlayerService.RawPokemonChoiceDTO one = new TournamentPlayerService.RawPokemonChoiceDTO(pokemonId, new ArrayList<>(moveNames),nature,ability);
            input.add(one);
        }

        return new TournamentPlayerService.JoinTournamentDTO(input.toArray(new TournamentPlayerService.RawPokemonChoiceDTO[0]));
    }

    @Test
    public void leaveTournament_success() throws JsonProcessingException, URISyntaxException {
        tournament= tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(0,10000),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusHours(1)
                )
        ));
        log.info("created tournament");

        Player player = (Player) userRepository.findById("fake_player").orElseThrow();
        Set<Move> movesAffected = new HashSet<>();

        assertEquals("abc", tournament.getName());
        assertEquals("fake_admin", tournament.getAdminUsername());

        log.info(new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsString(tournament));

        Team team = new Team(player,tournament);
        tournament.addTeam(team, ZonedDateTime.now());

        for(int pokemonId : pokemonsToJoinWith){
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();

            ChosenPokemon chosenPokemon = new ChosenPokemon(team,pokemon);
            chosenPokemon.setAbility(getRandomSetElement(pokemon.getAbilities()));

            Set<String> moveNames = new HashSet<>();
            Set<String> chosenMoves = new HashSet<>();

            pokemon.getMoves().forEach(move ->{
                moveNames.add(move.getName());
            });

            while (chosenMoves.size() < 4){
                chosenMoves.add(getRandomSetElement(moveNames));
            }

            chosenMoves.forEach(moveName ->{
                chosenPokemon.learnMove(moveRepository.findById(moveName).orElseThrow());
            });
            chosenPokemon.setNature(randomEnum(POKEMON_NATURE.class));
            team.addChosenPokemon(chosenPokemon);
        }
        // join

        team = teamRepository.save(team);

        // quit

        var initialAmount = chosenPokemonRepository.count();

        URIBuilder builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player","tournament", tournament.getId().toString(), "leave");

        var result = restTemplate.exchange(builder.build(),HttpMethod.DELETE,createStatefulResponse(username), Message.class);

        assertEquals(200, result.getStatusCode().value());

        assertTrue(teamRepository.findById(new Team.TeamId(player,tournament)).isEmpty());
        assertEquals(pokemonsToJoinWith.length,initialAmount- chosenPokemonRepository.count());
        for(int pokemonId : pokemonsToJoinWith) {
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();
            assertTrue(
                    chosenPokemonRepository.findById(new ChosenPokemon.PokemonId(team, pokemon)).isEmpty()
            );
        }

        for (Move m : movesAffected){
            assertFalse(moveRepository.findById(m.getName()).isEmpty());
        }
    }

    @Test
    public void searchTournament_success () throws URISyntaxException {
        Player player = playerRepository.findById(username).orElseThrow();
        player.setPoints(50.0);
        player.setEmail("wellFormedEmail@tryingtoevadebeanvalidation.com");
        player = playerRepository.save(player);

        URIBuilder builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player","tournament","search")
                .addParameter("limit","10")
                .addParameter("page","1")
                .addParameter("query","abc");

        var result = restTemplate.exchange(builder.build(),HttpMethod.GET,createStatefulResponse(username), TournamentPageDTO.class);

        assertEquals(200,result.getStatusCode().value());
        var initialMatch = result.getBody().count();
        tournamentRepository.save(
                new OpenTournament(
                        "abc",
                        (Admin) userRepository.findById("fake_admin").orElseThrow(),
                        new Tournament.EloLimit(0,45),
                        new Tournament.RegistrationPeriod(
                                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)
                        )
                )
        );
        tournamentRepository.save(
                new OpenTournament(
                        "ABC",
                        (Admin) userRepository.findById("fake_admin").orElseThrow(),
                        new Tournament.EloLimit(0,100),
                        new Tournament.RegistrationPeriod(
                                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)
                        )
                )
        );

        tournamentRepository.save(
                new OpenTournament(
                        "ABC",
                        (Admin) userRepository.findById("fake_admin").orElseThrow(),
                        new Tournament.EloLimit(100,200),
                        new Tournament.RegistrationPeriod(
                                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)
                        )
                )
        );

        result = restTemplate.exchange(builder.build(),HttpMethod.GET,createStatefulResponse(username), TournamentPageDTO.class);

        assertEquals(200,result.getStatusCode().value());
        assertEquals(1,result.getBody().count()-initialMatch);;
    }

    @Test
    public void joinClosedTournament_success() throws URISyntaxException {
        ClosedTournament t =  tournamentRepository.save(
                new ClosedTournament(
                        "abc",
                        (Admin) userRepository.findById("fake_admin").orElseThrow(),
                        new Tournament.EloLimit(0,50000),
                        new Tournament.RegistrationPeriod(
                                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)
                        )
                )
        );

        Player player = playerRepository.findById(username).orElseThrow();

        TournamentPlayerService.JoinTournamentDTO dto = generateJoinDTO();

        URIBuilder builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player", "tournament", t.getId().toString(), "join");

        var result = restTemplate.exchange(builder.build(), HttpMethod.POST, createStatefulResponse(username, dto), Message.class);

        assertEquals(400, result.getStatusCode().value());

        t.addInvitedPlayer(List.of(player));
        tournamentRepository.save(t);

        result = restTemplate.exchange(builder.build(), HttpMethod.POST, createStatefulResponse(username, dto), Message.class);

        assertEquals(200, result.getStatusCode().value());

    }

    @Test
    public void searchInvitedClosedTournaments_success() throws URISyntaxException {
        URIBuilder builder = new URIBuilder()
                .setHost(host)
                .setPort(port)
                .setScheme(scheme)
                .setPathSegments("player","tournament","closed","invited")
                .addParameter("limit","10")
                .addParameter("page","1");

        var result = restTemplate.exchange(builder.build(),HttpMethod.GET,createStatefulResponse(username), TournamentPageDTO.class);
        assertEquals(200, result.getStatusCode().value());
        long initialCount = result.getBody().count();

        Player player = playerRepository.findById(username).orElseThrow();
        List<ClosedTournament> tournaments = new ArrayList<>();
        for (int i =0; i< 10; i++) {
            tournaments.add(
                    tournamentRepository.save(
                            new ClosedTournament(
                                    "abc"+i,
                                    (Admin) userRepository.findById("fake_admin").orElseThrow(),
                                    new Tournament.EloLimit(0,50_000),
                                    new Tournament.RegistrationPeriod(
                                            ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)
                                    )
                            )
                    )
            );
        }
        for (long i =0; i< 10; i++) {
            for (int j =0 ; j<i ; j++){
                ClosedTournament t = tournaments.get(j);
                t.addInvitedPlayer(List.of(player));
                tournaments.set(j, tournamentRepository.save(t));
            }
            builder
                    .setPathSegments("player","tournament","closed","invited")
                    .addParameter("limit","10")
                    .addParameter("page","1");

            result = restTemplate.exchange(builder.build(),HttpMethod.GET,createStatefulResponse(username), TournamentPageDTO.class);
            assertEquals(200, result.getStatusCode().value());
            assertEquals(i, result.getBody().count()-initialCount);

        }

    }


    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    private static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(random.nextInt(set.size())).findFirst().orElse(null);
    }
}