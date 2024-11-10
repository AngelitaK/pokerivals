package com.smu.csd.pokerivals.tournament.service;


import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.pokemon.entity.Ability;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.entity.PokemonNature;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.tournament.dto.TournamentPageDTO;
import com.smu.csd.pokerivals.tournament.entity.ChosenPokemon;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.*;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Transactional
@PreAuthorize("hasAuthority('PLAYER')")
public class TournamentPlayerService {

    private final TournamentRepository tournamentRepository;
    private final DateFactory dateFactory;
    private final TournamentPagingRepository tournamentPagingRepository;
    private final PlayerRepository playerRepository;
    private final PokemonRepository pokemonRepository;
    private final TeamRepository teamRepository;
    private final ClosedTournamentPagingRepository closedTournamentPagingRepository;
    private final ClosedTournamentRepository closedTournamentRepository;

    @Autowired
    public TournamentPlayerService(TournamentRepository tournamentRepository, DateFactory dateFactory, TournamentPagingRepository tournamentPagingRepository, PlayerRepository playerRepository, PokemonRepository pokemonRepository, TeamRepository teamRepository, ClosedTournamentPagingRepository closedTournamentPagingRepository, ClosedTournamentRepository closedTournamentRepository) {
        this.tournamentRepository = tournamentRepository;
        this.dateFactory = dateFactory;
        this.tournamentPagingRepository = tournamentPagingRepository;
        this.playerRepository = playerRepository;
        this.pokemonRepository = pokemonRepository;
        this.teamRepository = teamRepository;
        this.closedTournamentPagingRepository = closedTournamentPagingRepository;
        this.closedTournamentRepository = closedTournamentRepository;
    }

    public record RawPokemonChoiceDTO(
            int pokemonId,
            List<String> moves,
            PokemonNature nature,
            String ability
    ){ }

    public record JoinTournamentDTO(
            RawPokemonChoiceDTO[] pokemonChoicesRaw
    ){}

    @PreAuthorize("hasAuthority('PLAYER')")
    public void joinTournament(String playerUsername, UUID tournamentID, JoinTournamentDTO dto){
        Player player = playerRepository.findById(playerUsername).orElseThrow();
        Tournament tournament = tournamentRepository.getTournamentById(tournamentID).orElseThrow();

        if (teamRepository.findById(new Team.TeamId(player,tournament)).isPresent()){
            throw new IllegalArgumentException("Player already registered");
        }

        Team team = new Team(player,tournament);


        for(RawPokemonChoiceDTO oneSelection : dto.pokemonChoicesRaw()){
            log.info(String.valueOf(oneSelection));
            Pokemon pokemon = pokemonRepository.findById(oneSelection.pokemonId()).orElseThrow();
            ChosenPokemon cp = new ChosenPokemon(team,pokemon);

            List<Move> moves = new ArrayList<>();
            AtomicReference<Ability> ability = new AtomicReference<>();

            pokemon.getMoves().forEach(move -> {
                if (oneSelection.moves().contains(move.getName())){
                    moves.add(move);
                }
            });
            if (moves.size() != oneSelection.moves().size()){
                throw new NoSuchElementException("Cannot Find Some Moves");
            }

            pokemon.getAbilities().forEach(a -> {
                if (oneSelection.ability().contains(a.getName())){
                    ability.set(a);
                }
            });

            if (ability.get() == null){
                throw new NoSuchElementException("Cannot find ability");
            }

            cp.setNature(oneSelection.nature());
            moves.forEach(cp::learnMove);
            cp.setAbility(ability.get());

            team.addChosenPokemon(cp);
        }
        log.info("saving team...");
        tournament.addTeam(team, dateFactory.getToday());
    }

    // can be executed by player or admin
    public void leaveTournament(String playerUsername, UUID tournamentID){
        Player player = playerRepository.findById(playerUsername).orElseThrow();
        Tournament tournament = tournamentRepository.getTournamentById(tournamentID).orElseThrow();
        if (!tournament.getRegistrationPeriod().contains(dateFactory.getToday())){
            throw new IllegalArgumentException("Player can only withdraw during registration period");
        }
        teamRepository.findById(new Team.TeamId(player,tournament)).orElseThrow();
        teamRepository.deleteById(new Team.TeamId(player,tournament));
    }

    public TournamentPageDTO getTournamentsByPlayer(String playerUsername, int page, int limit){
        return new TournamentPageDTO(
                tournamentPagingRepository.findTournamentByPlayerUsername(playerUsername, PageRequest.of(page,limit)),
                tournamentRepository.countTournamentByPlayerUsername(playerUsername)
        );
    }

    public TournamentPageDTO searchTournaments(String query,String playerUsername, int page, int limit){
        Player player = playerRepository.findById(playerUsername).orElseThrow();
        return new TournamentPageDTO(
                tournamentPagingRepository.searchTournaments(query,player.getPoints(), PageRequest.of(page,limit)),
                tournamentRepository.countSearchResult(query,player.getPoints())
        );
    }

    public TournamentPageDTO findTournamentWherePlayerIsInvited(String playerUsername, int page, int limit){
        Player player = playerRepository.findById(playerUsername).orElseThrow();
        return new TournamentPageDTO(
                closedTournamentPagingRepository.findClosedTournamentWherePlayerInvited(player.getUsername(), PageRequest.of(page,limit)),
                closedTournamentRepository.countClosedTournamentWherePlayerInvited(player.getUsername())
        );
    }

    public Team getMyTeam(String playerUsername, UUID tournamentID){
        return teamRepository.findById(new Team.TeamId(
                playerRepository.findById(playerUsername).orElseThrow(),
                tournamentRepository.findById(tournamentID).orElseThrow()
        )).orElseThrow();
    }

}
