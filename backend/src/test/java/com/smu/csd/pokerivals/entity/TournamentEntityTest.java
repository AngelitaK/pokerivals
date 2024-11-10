package com.smu.csd.pokerivals.entity;


import com.smu.csd.pokerivals.configuration.LoadData;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.POKEMON_NATURE;
import com.smu.csd.pokerivals.pokemon.repository.AbilityRepository;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.tournament.entity.ChosenPokemon;
import com.smu.csd.pokerivals.tournament.entity.OpenTournament;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.ChosenPokemonRepository;
import com.smu.csd.pokerivals.tournament.repository.TeamRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.ClanRepository;
import com.smu.csd.pokerivals.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.core.env.Environment;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/test",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TournamentEntityTest {
    @Autowired
    private TestEntityManager testEM;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChosenPokemonRepository chosenPokemonRepository;

    @Autowired
    private ClanRepository clanRepository;

    @Autowired
    private AbilityRepository abilityRepository;


    @Autowired
    private Environment environment;

    @BeforeEach
    public void loadData() throws Exception {
        new LoadData(environment).initDatabase(userRepository,clanRepository, pokemonRepository,abilityRepository, moveRepository).run("abc");
    }

    @Test
    public void testJoinAndLeaveTournament(){
        var tournament = tournamentRepository.save(new OpenTournament(
                "abc",
                (Admin) userRepository.findById("fake_admin").orElseThrow(),
                new Tournament.EloLimit(
                        0,5000
                ),
                new Tournament.RegistrationPeriod(
                        ZonedDateTime.now(),
                        ZonedDateTime.now().plusHours(1)
                )
        ));
        var player = (Player) userRepository.findById("fake_player").orElseThrow();

        Team team = createTeam(player,tournament);
        tournament.addTeam(team,ZonedDateTime.now());
        tournamentRepository.save(tournament);

        assertEquals(1,teamRepository.count());
        assertEquals(6,chosenPokemonRepository.count());

        assertTrue(teamRepository.findById(new Team.TeamId(player,tournament)).isPresent());

        teamRepository.deleteById(new Team.TeamId(player,tournament));


//        assertEquals(0,teamRepository.count());
        assertEquals(0,chosenPokemonRepository.count());

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

            Set<Move> moves = new HashSet<>();
            while (moves.size()<4){
                moves.add(pokemon.getMoves().stream().toList().get(random.nextInt(pokemon.getMoves().size())));
            }

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

    private Random random = ThreadLocalRandom.current();

    private <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
