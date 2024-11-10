package com.smu.csd.pokerivals.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.match.MatchService;
import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.PokemonNature;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.tournament.entity.ChosenPokemon;
import com.smu.csd.pokerivals.tournament.entity.OpenTournament;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.AdminRepository;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.*;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MatchSearchIntegrationTest {

    //tournament creation
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Mocked for google
    @MockBean
    private GoogleIdTokenVerifier verifier;

    @Mock
    private GoogleIdToken.Payload payload;

    @Mock
    private  GoogleIdToken idTokenMock;

    private String adminUsername;

    private String baseUrl ="http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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
        adminUsername = storeCookie(result);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("ADMIN",result.getBody().role());

        verify(verifier).verify(tokenMap.get(subOfPlayer));
        verify(idTokenMock).getPayload();

        reset(verifier,idTokenMock);
    }

    private String playerUsername;

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
        playerUsername = storeCookie(result);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody().role());
        assertEquals("PLAYER",result.getBody().role());

        verify(verifier).verify(tokenMap.get(subOfPlayer));
        verify(idTokenMock).getPayload();

        reset(verifier,idTokenMock);
    }

    private final Random random = ThreadLocalRandom.current();

    private <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    private Team createTeam(Player player, Tournament tournament){
        var team = new Team(player,tournament);

        var noOfPokemons = pokemonRepository.count();


        List<ChosenPokemon> chosenPokemons = new ArrayList<>();
        while (chosenPokemons.size() < 6){
            var pokemonIdChosen = random.nextLong(1,noOfPokemons+1);
            var pokemon = pokemonRepository.findById((int)pokemonIdChosen).orElseThrow();

            var nature = randomEnum(PokemonNature.class);
            var ability = pokemon.getAbilities().stream().toList().get(random.nextInt(pokemon.getAbilities().size()));

            var originalMoves = new ArrayList<>(pokemon.getMoves());
            if (originalMoves.size() < 4){
                continue;
            }
            Collections.shuffle(originalMoves);
            List<Move> moves = originalMoves.subList(0,4);


            var cp = new ChosenPokemon(team,pokemon);
            for(Move m : moves ){
                cp.learnMove(m);
            };
            cp.setNature(nature);
            cp.setAbility(ability);
            chosenPokemons.add(cp);
        }

        for (ChosenPokemon cp: chosenPokemons){
            team.addChosenPokemon(cp);
        }

        return team;
    }

    /**
     * Input for testBracket
     */
    private int numPlayers = 31;

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void searchForMatchAsAdmin_success() throws JsonProcessingException, URISyntaxException {
        var factory = restTemplate.getRestTemplate().getRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());

        Admin a1 = adminRepository.findById("fake_admin").orElseThrow();

        for (int ooo = 0; ooo< 2; ooo++) {
            // create an open tournament
            Tournament t1 = new OpenTournament("vgc",
                    a1,
                    new Tournament.EloLimit(
                            0,
                            50_000
                    ),
                    new Tournament.RegistrationPeriod(
                            ZonedDateTime.now(),
                            ZonedDateTime.now().plusHours(1)
                    ));
            t1 = tournamentRepository.save(t1);

            // create players and teams with points of 0, 100, 200 etc
            for (int i = 0; i < numPlayers; i++) {
                Player toAdd = new Player("player" + i, "p" + i);
                toAdd.setPoints(i * 100);
                if (playerRepository.findById("player" + i).isEmpty()) {
                    toAdd = playerRepository.save(toAdd);
                } else {
                    toAdd = playerRepository.findById("player" + i).orElseThrow();
                    toAdd.setPoints(i * 100);
                    toAdd = playerRepository.save(toAdd);
                }

                Team teamToAdd = createTeam(toAdd, t1);
                t1.addTeam(teamToAdd, ZonedDateTime.now());

                t1 = tournamentRepository.save(t1);
            }

            t1.setRegistrationPeriod(
                    new Tournament.RegistrationPeriod(
                            ZonedDateTime.now().minusHours(2),
                            ZonedDateTime.now().minusHours(1)
                    )
            );

            t1 = tournamentRepository.save(t1);

            Tournament finalT = t1;
            assertThrows(IllegalArgumentException.class,()->{
                matchService.startSingleEliminationTournament(finalT.getId(),"abc");
            });
            matchService.startSingleEliminationTournament(finalT.getId(),"fake_admin");

            List<Match> matches = matchRepository.findByMatchIdTournamentId(t1.getId());

            ZonedDateTime today = ZonedDateTime.now();

            ZonedDateTime firstDay = LocalDate.now().with(firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime lastDay = LocalDate.now().plusMonths(1).with(firstDayOfMonth()).atStartOfDay().atZone(ZoneId.systemDefault());
            var initialPage = matchService.getMatchesForAdminBetweenDates("fake_admin", firstDay,lastDay,0,10);

            ZonedDateTime firstDayOfLastMonth = LocalDate.now().minusMonths(1).with(firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault());

            int totalNoOfMatches = matches.size();
            int targetNoOfMatchBeforeStartDay = 10;
            int targetNoOfMatchAfterEndDay = 20;

            int noOfMatchBeforeStartDay = 0; // max 20
            int noOfMatchAfterEndDay = 0;

            for (Match match : matches){
                if (!match.getMatchResult().equals(MatchResult.PENDING)){
                    totalNoOfMatches--;
                    continue;
                }
                if (noOfMatchAfterEndDay < targetNoOfMatchAfterEndDay){
                    match.setTimeMatchOccursAndResetAgreement(lastDay.plusDays(5),today);
                    noOfMatchAfterEndDay++;
                } else if (noOfMatchBeforeStartDay < targetNoOfMatchBeforeStartDay ){
                    noOfMatchBeforeStartDay++;
                    match.setTimeMatchOccursAndResetAgreement(firstDay.minusDays(5),firstDayOfLastMonth);
                } else {
                    match.setTimeMatchOccursAndResetAgreement(today, firstDayOfLastMonth);
                }
            }

            matches =  matchRepository.saveAll(matches);

            int totalRecountNoOfMatches = matches.size();
            int noOfMatchBeforeStartDayRecount = 0;
            int noOfMatchAfterEndDayRecount = 0;

            for (Match match : matches){
                if (!match.getMatchResult().equals(MatchResult.PENDING)){
                    totalRecountNoOfMatches--;
                    continue;
                }
                if (match.getTimeMatchOccurs().isBefore(firstDay)){
                    noOfMatchBeforeStartDayRecount++;
                } else if (match.getTimeMatchOccurs().isAfter(lastDay)) {
                    noOfMatchAfterEndDayRecount++;
                } else {
                    assertEquals(match.getTimeMatchOccurs().toInstant().truncatedTo(ChronoUnit.SECONDS), today.toInstant().truncatedTo(ChronoUnit.SECONDS));
                }
            }

            assertEquals(totalNoOfMatches, totalRecountNoOfMatches);
            assertEquals(noOfMatchAfterEndDay,noOfMatchAfterEndDayRecount);
            assertEquals(noOfMatchBeforeStartDay,noOfMatchBeforeStartDayRecount);

            var page =  matchService.getMatchesForAdminBetweenDates(adminUsername, firstDay,lastDay,0,10);

            assertEquals(totalNoOfMatches-noOfMatchAfterEndDay-noOfMatchBeforeStartDay,page.count()-initialPage.count());
            //  assertEquals(totalNoOfMatches-noOfMatchAfterEndDay-noOfMatchBeforeStartDay,page.matches().size());

            URIBuilder startMatchUriBuilder =
                    new URIBuilder().setHost("localhost")
                            .setScheme("http")
                            .setPort(port)
                            .setPathSegments("tournament","match", "me", "admin")
                            .setParameters(List.of(
                                    new BasicNameValuePair("start",firstDay.toOffsetDateTime().toString()),
                                    new BasicNameValuePair("end",lastDay.toOffsetDateTime().toString()),
                                    new BasicNameValuePair("page", "0"),
                                    new BasicNameValuePair("limit", "10")
                            ));

            var result = restTemplate.exchange(startMatchUriBuilder.build(), HttpMethod.GET, createStatefulResponse(adminUsername), MatchService.MatchPageDTO.class);
            assertEquals(200, result.getStatusCode().value());
            assertEquals(totalNoOfMatches-noOfMatchAfterEndDay-noOfMatchBeforeStartDay,result.getBody().count()-initialPage.count());
        }


    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "PLAYER"})
    public void searchForMatchAsPlayer_success() throws JsonProcessingException, URISyntaxException {
        var factory = restTemplate.getRestTemplate().getRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());

        Admin a1 = adminRepository.findById("fake_admin").orElseThrow();
        Player p = playerRepository.findById("fake_player").orElseThrow();

        for (int ooo = 0; ooo< 2; ooo++) {
            // create an open tournament
            Tournament t1 = new OpenTournament("vgc",
                    a1,
                    new Tournament.EloLimit(
                            0,
                            50_000
                    ),
                    new Tournament.RegistrationPeriod(
                            ZonedDateTime.now(),
                            ZonedDateTime.now().plusHours(1)
                    ));
            t1 = tournamentRepository.save(t1);

            // create players and teams with points of 0, 100, 200 etc
            for (int i = 0; i < numPlayers; i++) {
                Player toAdd = new Player("player" + i, "p" + i);
                toAdd.setPoints(i * 100);
                if (playerRepository.findById("player" + i).isEmpty()) {
                    toAdd = playerRepository.save(toAdd);
                } else {
                    toAdd = playerRepository.findById("player" + i).orElseThrow();
                    toAdd.setPoints(i * 100);
                    toAdd = playerRepository.save(toAdd);
                }

                Team teamToAdd = createTeam(toAdd, t1);
                t1.addTeam(teamToAdd, ZonedDateTime.now());

                t1 = tournamentRepository.save(t1);
            }

            var fakePlayerTeam = createTeam(p, t1);
            t1.addTeam(fakePlayerTeam, ZonedDateTime.now());

            t1.setRegistrationPeriod(
                    new Tournament.RegistrationPeriod(
                            ZonedDateTime.now().minusHours(2),
                            ZonedDateTime.now().minusHours(1)
                    )
            );

            t1 = tournamentRepository.save(t1);

            Tournament finalT = t1;
            assertThrows(IllegalArgumentException.class,()->{
                matchService.startSingleEliminationTournament(finalT.getId(),"abc");
            });
            matchService.startSingleEliminationTournament(finalT.getId(),"fake_admin");

            List<Match> matches = matchRepository.findByMatchIdTournamentId(t1.getId());

            ZonedDateTime today = ZonedDateTime.now();

            ZonedDateTime firstDay = LocalDate.now().with(firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime lastDay = LocalDate.now().plusMonths(1).with(firstDayOfMonth()).atStartOfDay().atZone(ZoneId.systemDefault());
            var initialPage = matchService.getMatchesForPlayerBetweenDates(playerUsername, firstDay,lastDay,0,10);

            ZonedDateTime firstDayOfLastMonth = LocalDate.now().minusMonths(1).with(firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault());

            int totalNoOfMatches = matches.size();
            int targetNoOfMatchBeforeStartDay = 10;
            int targetNoOfMatchAfterEndDay = 20;

            int noOfMatchBeforeStartDay = 0; // max 20
            int noOfMatchAfterEndDay = 0;


            int i =-1;
            for (Match match : matches){
                i++;
                if (!match.getMatchResult().equals(MatchResult.PENDING)){
                    totalNoOfMatches--;
                    continue;
                }
                // add this player
                if (i %2 ==0 &&  match.getTeamA()== null){
                    match.finaliseTeamA(fakePlayerTeam,today);
                } else if (i %2 ==1 &&  match.getTeamB()== null) {
                    match.finaliseTeamB(fakePlayerTeam,today);
                } else {
                    totalNoOfMatches--;
                    continue;
                }
                if (noOfMatchAfterEndDay < targetNoOfMatchAfterEndDay){
                    match.setTimeMatchOccursAndResetAgreement(lastDay.plusDays(5),today);
                    noOfMatchAfterEndDay++;
                } else if (noOfMatchBeforeStartDay < targetNoOfMatchBeforeStartDay ){
                    noOfMatchBeforeStartDay++;
                    match.setTimeMatchOccursAndResetAgreement(firstDay.minusDays(5),firstDayOfLastMonth);
                } else {
                    match.setTimeMatchOccursAndResetAgreement(today, firstDayOfLastMonth);
                }

            }

            matches =  matchRepository.saveAll(matches);

            int totalRecountNoOfMatches = matches.size();
            int noOfMatchBeforeStartDayRecount = 0;
            int noOfMatchAfterEndDayRecount = 0;

            for (Match match : matches){
                if (match.getTimeMatchOccurs() == null ||
                        !match.getMatchResult().equals(MatchResult.PENDING)
                        ||
                        ( !Objects.equals(match.getTeamA(),fakePlayerTeam)
                                &&
                                !Objects.equals(match.getTeamB(),fakePlayerTeam)
                        ) ){
                    totalRecountNoOfMatches--;
                    continue;
                }
                if (match.getTimeMatchOccurs().isBefore(firstDay)){
                    noOfMatchBeforeStartDayRecount++;
                } else if (match.getTimeMatchOccurs().isAfter(lastDay)) {
                    noOfMatchAfterEndDayRecount++;
                } else {
                    assertEquals(match.getTimeMatchOccurs().toInstant().truncatedTo(ChronoUnit.SECONDS), today.toInstant().truncatedTo(ChronoUnit.SECONDS));
                }
            }

            assertEquals(totalNoOfMatches, totalRecountNoOfMatches);
            assertEquals(noOfMatchAfterEndDay,noOfMatchAfterEndDayRecount);
            assertEquals(noOfMatchBeforeStartDay,noOfMatchBeforeStartDayRecount);

            var page =  matchService.getMatchesForPlayerBetweenDates(playerUsername, firstDay,lastDay,0,10);

            assertEquals(totalNoOfMatches-noOfMatchAfterEndDay-noOfMatchBeforeStartDay,page.count()-initialPage.count());
            //  assertEquals(totalNoOfMatches-noOfMatchAfterEndDay-noOfMatchBeforeStartDay,page.matches().size());

            URIBuilder startMatchUriBuilder =
                    new URIBuilder().setHost("localhost")
                            .setScheme("http")
                            .setPort(port)
                            .setPathSegments("tournament","match", "me", "player")
                            .setParameters(List.of(
                                    new BasicNameValuePair("start",firstDay.toOffsetDateTime().toString()),
                                    new BasicNameValuePair("end",lastDay.toOffsetDateTime().toString()),
                                    new BasicNameValuePair("page", "0"),
                                    new BasicNameValuePair("limit", "10")
                            ));

            var result = restTemplate.exchange(startMatchUriBuilder.build(), HttpMethod.GET, createStatefulResponse(playerUsername), MatchService.MatchPageDTO.class);
            assertEquals(200, result.getStatusCode().value());
            assertEquals(totalNoOfMatches-noOfMatchAfterEndDay-noOfMatchBeforeStartDay,result.getBody().count()-initialPage.count());
        }


    }


}
