package com.smu.csd.pokerivals.entity;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.match.MatchService;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.entity.MatchWrapper;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.POKEMON_NATURE;
import com.smu.csd.pokerivals.pokemon.repository.AbilityRepository;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.tournament.entity.ChosenPokemon;
import com.smu.csd.pokerivals.tournament.entity.OpenTournament;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TeamRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TournamentSeedingTest {

    //tournament creation

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private PlayerPagingRepository playerPagingRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private MatchService matchService;

    //------------------------- for player service-----------------
    @Autowired
    private PlayerRepository playerRepository;

    @MockBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    private ClanRepository clanRepository;
    //------------------------------------------------------------------------

    @Autowired
    private MatchRepository matchRepository;

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

            var nature = randomEnum(POKEMON_NATURE.class);
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
    private int numPlayers = 13;

    @Test
    public void testBracket(){
        matchRepository.deleteAll();

        Admin a1 = adminRepository.findById("fake_admin").orElseThrow();

        // create an open tournament
        Tournament t1 = new OpenTournament("vgc" , a1);
        t1 = tournamentRepository.save(t1);

        // create 8 players and teams with points of 0, 100, 200 etc
        for (int i = 0 ; i < numPlayers ; i++){
            Player toAdd = new Player("player" + i , "p" + i);
            toAdd.setPoints(i * 100);
            if (playerRepository.findById("player"+i).isEmpty()){
                toAdd = playerRepository.save(toAdd);
            } else {
                toAdd= playerRepository.findById("player"+i).orElseThrow();
            }

            Team teamToAdd = createTeam(toAdd,t1);
            t1.addTeam(teamToAdd, ZonedDateTime.now());

            tournamentRepository.save(t1);
        }

        var matches =  matchService.startSingleElimTournament(t1.getId(), ZonedDateTime.now());
        System.out.println(MatchWrapper.reconstructTree(matches));

//        var match = matchRepository.findById( new Match.MatchId(t1.getId(),3,4)).orElseThrow();
//        assertNotNull(match);
//        assertEquals(800.0, match.getTeamA().getPlayer().getPoints());
//        assertEquals(100.0, match.getTeamB().getPlayer().getPoints());

        System.out.println(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(t1.getId())));

        // tests
        matchService.advance(t1.getId(),3,7,MatchResult.CANCELLED,ZonedDateTime.now());

        matchService.advance(t1.getId(),3,3, MatchResult.TEAM_A,ZonedDateTime.now());
        System.out.println(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(t1.getId())));

        matchService.advance(t1.getId(),3,4, MatchResult.TEAM_B,ZonedDateTime.now());

        matchService.forfeit(t1.getId(),3,6,false,ZonedDateTime.now());

        matchService.advance(t1.getId(),3,5,MatchResult.TEAM_A, ZonedDateTime.now());

        System.out.println(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(t1.getId())));

        Tournament finalT = t1;
        assertThrows(IllegalArgumentException.class, ()->{
            matchService.advance(finalT.getId(),2,0,MatchResult.CANCELLED,ZonedDateTime.now());
        });
        matchService.advance(t1.getId(),2,3, MatchResult.TEAM_B,ZonedDateTime.now());
        matchService.advance(t1.getId(),2,1, MatchResult.TEAM_A,ZonedDateTime.now());
        matchService.forfeit(t1.getId(),2,2, true,ZonedDateTime.now());

        System.out.println(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(t1.getId())));

        matchService.advance(t1.getId(),1,0, MatchResult.TEAM_A,ZonedDateTime.now());
        matchService.advance(t1.getId(),1,1, MatchResult.TEAM_A,ZonedDateTime.now());

        System.out.println(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(t1.getId())));

        matchService.advance(t1.getId(),0,0, MatchResult.TEAM_A,ZonedDateTime.now());

        System.out.println(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(t1.getId())));
    }

}
