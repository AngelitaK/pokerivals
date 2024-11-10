package com.smu.csd.pokerivals.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smu.csd.pokerivals.configuration.LoadData;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.POKEMON_NATURE;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.AbilityRepository;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.tournament.entity.*;
import com.smu.csd.pokerivals.tournament.repository.ChosenPokemonRepository;
import com.smu.csd.pokerivals.tournament.repository.TeamRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.repository.ClanRepository;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
@ActiveProfiles("test")
public class TournamentEntityTest {
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final ChosenPokemonRepository chosenPokemonRepositor;
    private final UserRepository userRepository;
    private final ClanRepository clanRepository;
    private final MoveRepository moveRepository;
    private final AbilityRepository abilityRepository;
    private final PokemonRepository pokemonRepository;
    private UUID closedTournamentId;

    @Autowired
    public TournamentEntityTest(TournamentRepository tournamentRepository, TeamRepository teamRepository, ChosenPokemonRepository chosenPokemonRepositor, UserRepository userRepository, ClanRepository clanRepository, MoveRepository moveRepository, AbilityRepository abilityRepository, PokemonRepository pokemonRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.chosenPokemonRepositor = chosenPokemonRepositor;
        this.userRepository = userRepository;
        this.clanRepository = clanRepository;
        this.moveRepository = moveRepository;
        this.abilityRepository = abilityRepository;
        this.pokemonRepository = pokemonRepository;
    }

    @Autowired
    private Environment environment;

    private UUID openTournamentId;

    private int[] pokemonsToJoinWith = new int[]{
            2,4,6,3,7
    };

    @BeforeEach
    public void run() throws Exception {
        new LoadData(environment).initDatabase(userRepository, clanRepository, pokemonRepository, abilityRepository, moveRepository).run();
        openTournamentId = tournamentRepository.save(new OpenTournament("meow",(Admin) userRepository.findById("fake_admin").orElseThrow())).getId();
        closedTournamentId = tournamentRepository.save(new ClosedTournament("woof",(Admin) userRepository.findById("fake_admin").orElseThrow())).getId();
    }

    @Test
    public void testJoinOpenTournament() throws JsonProcessingException {
        Player player = (Player) userRepository.findById("fake_player").orElseThrow();
        Set<Move> movesAffected = new HashSet<>();

        assertNotNull(openTournamentId);
        Tournament tournament = tournamentRepository.getTournamentById(openTournamentId).orElseThrow();
        assertEquals("meow", tournament.getName());
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

            while (chosenMoves.size() < 5){
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
        assertEquals("fake_player", team.getPlayer().getUsername());
        assertEquals("meow", team.getTournament().getName());

        assertEquals(pokemonsToJoinWith.length,chosenPokemonRepositor.count());
        for(int pokemonId : pokemonsToJoinWith) {
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();
            assertEquals(
                    new Team.TeamId(player, tournament),
                    chosenPokemonRepositor.findById(new ChosenPokemon.PokemonId(team, pokemon)).orElseThrow().getPokemonId().getTeamId()
            );
        }

        // quit

        teamRepository.deleteById(new Team.TeamId(player,tournament));

        assertTrue(teamRepository.findById(new Team.TeamId(player,tournament)).isEmpty());
        assertEquals(0,chosenPokemonRepositor.count());
        for(int pokemonId : pokemonsToJoinWith) {
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();
            assertTrue(
                    chosenPokemonRepositor.findById(new ChosenPokemon.PokemonId(team, pokemon)).isEmpty()
            );
        }

        for (Move m : movesAffected){
            assertFalse(moveRepository.findById(m.getName()).isEmpty());
        }
    }

    @Test
    public void testJoinClosedTournament(){
        Player player = (Player) userRepository.findById("fake_player").orElseThrow();
        Set<Move> movesAffected = new HashSet<>();

        assertNotNull(closedTournamentId);
        ClosedTournament tournament = (ClosedTournament) tournamentRepository.getTournamentById(closedTournamentId).orElseThrow();
        assertEquals("woof", tournament.getName());
        assertEquals("fake_admin", tournament.getAdminUsername());

        Team team = new Team(player,tournament);
        Team finalTeam = team;
        assertThrows(IllegalArgumentException.class, ()->{
            tournament.addTeam(finalTeam, ZonedDateTime.now());
        });
        tournament.addInvitedPlayer(List.of(player));
        tournament.addTeam(team, ZonedDateTime.now());

        for(int pokemonId : pokemonsToJoinWith){
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();

            ChosenPokemon chosenPokemon = new ChosenPokemon(team,pokemon);
            chosenPokemon.setAbility(getRandomSetElement(pokemon.getAbilities()));

            Set<Move> movesAdded = new HashSet<>();
            Set<String> moveNames = new HashSet<>();
            Set<String> chosenMoves = new HashSet<>();

            pokemon.getMoves().forEach(move ->{
                moveNames.add(move.getName());
            });

            while (chosenMoves.size() < 5){
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
        assertEquals("fake_player", team.getPlayer().getUsername());
        assertEquals("woof", team.getTournament().getName());

        assertEquals(pokemonsToJoinWith.length,chosenPokemonRepositor.count());
        for(int pokemonId : pokemonsToJoinWith) {
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();
            assertEquals(
                    new Team.TeamId(player, tournament),
                    chosenPokemonRepositor.findById(new ChosenPokemon.PokemonId(team, pokemon)).orElseThrow().getPokemonId().getTeamId()
            );
        }

        // quit

        teamRepository.deleteById(new Team.TeamId(player,tournament));

        assertTrue(teamRepository.findById(new Team.TeamId(player,tournament)).isEmpty());
        assertEquals(0,chosenPokemonRepositor.count());
        for(int pokemonId : pokemonsToJoinWith) {
            Pokemon pokemon = pokemonRepository.findById(pokemonId).orElseThrow();
            assertTrue(
                    chosenPokemonRepositor.findById(new ChosenPokemon.PokemonId(team, pokemon)).isEmpty()
            );
        }

        for (Move m : movesAffected){
            assertFalse(moveRepository.findById(m.getName()).isEmpty());
        }
    }

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    public static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(random.nextInt(set.size())).findFirst().orElse(null);
    }


}
