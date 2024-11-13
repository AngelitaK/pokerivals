package com.smu.csd.pokerivals.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.betting.dto.CompletedMatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.entity.BettingSide;
import com.smu.csd.pokerivals.betting.entity.DepositTransaction;
import com.smu.csd.pokerivals.betting.repository.DepositTransactionRepository;
import com.smu.csd.pokerivals.betting.repository.TransactionRepository;
import com.smu.csd.pokerivals.betting.service.AdminBettingService;
import com.smu.csd.pokerivals.betting.service.PlayerBettingService;
import com.smu.csd.pokerivals.match.MatchService;
import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.PokemonNature;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.security.AuthenticationController;
import com.smu.csd.pokerivals.tournament.entity.ChosenPokemon;
import com.smu.csd.pokerivals.tournament.entity.OpenTournament;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.tournament.service.TournamentAdminService;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.AdminRepository;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.storeCookie;
import static com.smu.csd.pokerivals.integration.IntegrationTestDependency.tokenMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"non-async", "test"})
//@ActiveProfiles("non-async")
public class BettingIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

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

    private String playerUsername;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    private String adminUsername;

    @Autowired
    private PokemonRepository pokemonRepository;
    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentAdminService tournamentAdminService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AdminBettingService adminBettingService;
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

    }

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
        assertEquals("ADMIN", result.getBody().role());

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

    @WithMockUser(authorities = {"ADMIN"})
    public UUID startATournament(){
        var numPlayers = 32;
        Admin a1 = adminRepository.findById(adminUsername).orElseThrow();

        // create an open tournament
        Tournament t1 = new OpenTournament("vgc" ,
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
        for (int i = 0 ; i < numPlayers ; i++){
            Player toAdd = new Player("player" + i , "p" + i);
            toAdd.setPoints(i * 100);
            if (playerRepository.findById("player"+i).isEmpty()){
                toAdd = playerRepository.save(toAdd);
            } else {
                toAdd= playerRepository.findById("player"+i).orElseThrow();
                toAdd.setPoints(i*100);
                toAdd=playerRepository.save(toAdd);
            }

            Team teamToAdd = createTeam(toAdd,t1);
            t1.addTeam(teamToAdd, ZonedDateTime.now());

            t1=tournamentRepository.save(t1);
        }

        t1.setRegistrationPeriod(
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now().minusHours(2),
                        ZonedDateTime.now().minusHours(1)
                )
        );

        t1 = tournamentRepository.save(t1);

        matchService.startSingleEliminationTournament(t1.getId(),adminUsername);

        return t1.getId();
    }

    public Map<Boolean,List<Match.MatchId>> sortMatches(UUID tournamentId){
        Map<Boolean, List<Match.MatchId>> result = new HashMap<>();
        result.put(true, new ArrayList<>());
        result.put(false, new ArrayList<>());

        var matches=  matchRepository.findByMatchIdTournamentId(tournamentId);

        matches.forEach(m ->{
            result.get(
                    m.isBothTeamsFinalisedAndNotNull() && !m.isForfeited() && m.getMatchResult() == MatchResult.PENDING
            ).add(m.getMatchId());
        });
        return result;
    }

    public List<String> createBetters(int qty){
        List<String> result = new ArrayList<>();
        // create players and teams with points of 0, 100, 200 etc
        Session session = mock(Session.class);
        for (int i = 0 ; i < qty ; i++){
            Player toAdd = new Player("betting_player" + i , "bp" + i);
            toAdd.setEmail("joshlife333@gmail.com");
            if (playerRepository.findById("betting_player"+i).isEmpty()){
                toAdd = playerRepository.save(toAdd);
            } else {
                toAdd= playerRepository.findById("betting_player"+i).orElseThrow();
                toAdd.setEmail("joshlife333@gmail.com");
                toAdd=playerRepository.save(toAdd);
            }
            var uuid = UUID.randomUUID().toString();
            when(session.getId()).thenReturn(uuid);
            var deposit = new DepositTransaction(session, toAdd, ZonedDateTime.now() );
            deposit.confirm("joshlife333@gmail.com",uuid,"complete", 100_00);
            var initialBalance =transactionRepository.getPlayerBalance("betting_player" + i);
            depositTransactionRepository.save(
                    deposit
            );
            result.add(toAdd.getUsername());
            assertEquals(100_00, transactionRepository.getPlayerBalance("betting_player" + i)-initialBalance);
        }
        return  result;
    }

    @Autowired
    private PlayerBettingService playerBettingService;

    @Test
    @WithMockUser(authorities = {"ADMIN","PLAYER"})
    public void betMaking_success() throws JsonProcessingException {
        var result = adminBettingService.getSummariesOfOngoingBets(0, 10);
        log.info(objectMapper.writeValueAsString(result));

        var tournamentId = startATournament();
        var sortMatches = sortMatches(tournamentId);

        var bettersUsername = createBetters(10);

        class BetterDetails {
            private int numberOfABets;
            private int numberOfBBets;
            private int amountOfABetsInCents;
            private int amountOfBBetsInCents;
        }
        Map<String, BetterDetails> ref = new HashMap<>();
        bettersUsername.forEach(b -> {
            ref.put(b, new BetterDetails());
        });

        assertFalse(sortMatches.get(true).isEmpty());
        assertFalse(sortMatches.get(false).isEmpty());

        log.info(String.valueOf(sortMatches.get(true).size()));
        // Test 1 : making bad on unbettable matches
        sortMatches.get(false).forEach(unbettableMatch -> {
            assertThrows(IllegalArgumentException.class, () -> {
                playerBettingService.placeBet(bettersUsername.get(0), new PlayerBettingService.PlaceOrModifyBetDTO(
                        unbettableMatch, 5_00, BettingSide.TEAM_A
                ));
            });
        });

        int maxToBetPerMatch = 100_00 / sortMatches.get(true).size();
        // Test 2 :making bets
        int i = 0;
        sortMatches.get(true).forEach(bettableMatch -> {
            log.info("===================={}===================", bettableMatch);


            ref.forEach((k, v) -> {
                log.info("===================={}===================", k);
                log.info("Prediction win for me is for A {} for B {}",
                        playerBettingService.predictWinAmount(
                                new PlayerBettingService.PlaceOrModifyBetDTO(
                                        bettableMatch, 10_00, BettingSide.TEAM_A
                                )
                        ).winAmountInCents()
                        ,
                        playerBettingService.predictWinAmount(
                                new PlayerBettingService.PlaceOrModifyBetDTO(
                                        bettableMatch, 10_00, BettingSide.TEAM_B
                                )
                        ).winAmountInCents());
                if (random.nextBoolean()) {
                    int betAmt = random.nextInt(1_00, maxToBetPerMatch);
                    var side = random.nextBoolean() ? BettingSide.TEAM_B : BettingSide.TEAM_A;
                    playerBettingService.placeBet(k,
                            new PlayerBettingService.PlaceOrModifyBetDTO(
                                    bettableMatch, betAmt,
                                    side
                            )
                    );

                    if (side == BettingSide.TEAM_A) {
                        v.amountOfABetsInCents += betAmt;
                        v.numberOfABets++;
                    } else {
                        v.amountOfBBetsInCents += betAmt;
                        v.numberOfBBets++;
                    }
                    log.info("Im betting {} for {}", betAmt, side);
                }
            });
        });

        int noOfBetsMade = ref.values().stream().mapToInt(x -> x.numberOfABets + x.numberOfBBets).sum();

        sortMatches.get(true).forEach(m -> {
            matchService.setMatchTiming(adminUsername, new MatchService.SetMatchTimingDTO(
                    m, ZonedDateTime.now().plusHours(2)
            ));
        });

        List<MatchBettingSummaryDTO> list = new ArrayList<>();
        long noOfRecordsInCall = 1000000;
        int loopCount = 0;
        while (list.size() < noOfRecordsInCall) {
            result = adminBettingService.getSummariesOfOngoingBets(loopCount, 10);
            if (result.summaries().isEmpty()) {
                break;
            }
            noOfRecordsInCall = result.count();
            list.addAll(result.summaries());
            loopCount++;
        }
        System.out.println("===========================================");
        System.out.println(objectMapper.writeValueAsString(list.subList(0, sortMatches.get(true).size())));
        System.out.println("===========================================");

        assertEquals(noOfRecordsInCall, list.size());
        list = list.stream().filter(x -> x.match().getMatchId().getTournamentId().equals(tournamentId)).toList();

        // sum no of bets for A
        int noOfBetsForARef = ref.values().stream().mapToInt(
                x -> x.numberOfABets
        ).sum();
        int noOfBetsForBRef = ref.values().stream().mapToInt(
                x -> x.numberOfBBets
        ).sum();
        int centsOfBetsForARef = ref.values().stream().mapToInt(
                x -> x.amountOfABetsInCents
        ).sum();
        int centsOfBetsForBRef = ref.values().stream().mapToInt(
                x -> x.amountOfBBetsInCents
        ).sum();

        long noOfBetsForATest = list.stream().mapToLong(
                MatchBettingSummaryDTO::totalNoOfTeamABets
        ).sum();
        long noOfBetsForBTest = list.stream().mapToLong(
                MatchBettingSummaryDTO::totalNoOfTeamBBets
        ).sum();
        long centsOfBetsForATest = list.stream().mapToLong(
                MatchBettingSummaryDTO::totalBetsOnTeamAInCents
        ).sum();
        long centsOfBetsForBTest = list.stream().mapToLong(
                MatchBettingSummaryDTO::totalBetsOnTeamBInCents
        ).sum();

        assertEquals(noOfBetsForARef, noOfBetsForATest);
        assertEquals(noOfBetsForBRef, noOfBetsForBTest);
        assertEquals(centsOfBetsForATest, centsOfBetsForARef);
        assertEquals(centsOfBetsForBTest, centsOfBetsForBRef);

        // test forfeit
        var listOfMatches = sortMatches.get(true);
        Collections.shuffle(listOfMatches);
        var toForfeit = listOfMatches.subList(0, listOfMatches.size() / 2);
        var toWin = listOfMatches.subList(listOfMatches.size() / 2, listOfMatches.size());

        toForfeit.forEach(
                x -> {
                    matchService.forfeit(new MatchService.ForfeitDTO(
                            x, random.nextBoolean()
                    ), adminUsername);
                }
        );

        List<CompletedMatchBettingSummaryDTO> list2 = new ArrayList<>();
        noOfRecordsInCall = 1000000;
        loopCount = 0;
        while (list2.size() < noOfRecordsInCall) {
            var result2 = adminBettingService.getSummariesOfPastBets(loopCount, 10);
            if (result2.summaries().isEmpty()) {
                break;
            }
            noOfRecordsInCall = result2.count();
            list2.addAll(result2.summaries());
            loopCount++;
        }
        System.out.println("===========================================");
        System.out.println(objectMapper.writeValueAsString(list2));
        System.out.println("===========================================");

        noOfBetsForARef = 0;
        noOfBetsForBRef = 0;
        for (var x : toWin) {
            var aWins = random.nextBoolean();
            matchService.setMatchResult(new MatchService.SetMatchResultDTO(
                    x, aWins ? MatchResult.TEAM_A : MatchResult.TEAM_B
            ), adminUsername);
            var i1 = aWins ? noOfBetsForARef++ : noOfBetsForBRef++;
        }

        list2 = new ArrayList<>();
        noOfRecordsInCall = 1000000;
        loopCount =0;
        while(list.size() < noOfRecordsInCall){
            var result2 = adminBettingService.getSummariesOfPastBets(loopCount,10);
            if (result2.summaries().isEmpty()){
                break;
            }
            noOfRecordsInCall = result2.count();
            list2.addAll(result2.summaries());
            loopCount++;
        }
        System.out.println("===========================================");
        System.out.println(objectMapper.writeValueAsString(list2));
        System.out.println("===========================================");
        assertEquals(noOfRecordsInCall,list2.size());


        list2 = list2.stream().filter(x -> x.match().getMatchId().getTournamentId().equals(tournamentId)).toList();

        assertEquals(toWin.size(), list2.size());

        noOfBetsForATest = list2.stream().filter(
                x -> x.match().getMatchResult() == MatchResult.TEAM_A
        ).count();
        noOfBetsForBTest = list2.stream().filter(
                x -> x.match().getMatchResult() == MatchResult.TEAM_B
        ).count();

        assertEquals(noOfBetsForARef, noOfBetsForATest);
        assertEquals(noOfBetsForBRef, noOfBetsForBTest);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public  void abc() throws JsonProcessingException {
        List<CompletedMatchBettingSummaryDTO> list2 = new ArrayList<>();
        var noOfRecordsInCall = 1000000L;
        var loopCount =0L;
        while(list2.size() < noOfRecordsInCall){
            var result2 = adminBettingService.getSummariesOfPastBets((int) loopCount,10);
            if (result2.summaries().isEmpty()){
                break;
            }
            noOfRecordsInCall = result2.count();
            list2.addAll(result2.summaries());
            loopCount++;
        }
        System.out.println("===========================================");
        System.out.println(objectMapper.writeValueAsString(list2));
        System.out.println("===========================================");

        assertEquals(noOfRecordsInCall,list2.size());
    }
}
