package com.smu.csd.pokerivals.tournament.service;


import com.smu.csd.pokerivals.NotificationService;
import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.tournament.dto.TournamentPageDTO;
import com.smu.csd.pokerivals.tournament.entity.ClosedTournament;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TeamPagingRepository;
import com.smu.csd.pokerivals.tournament.repository.TeamRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentPagingRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.AdminRepository;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@PreAuthorize("hasAuthority('ADMIN')")
@Transactional
public class TournamentAdminService {

    private final AdminRepository adminRepository;
    private final TournamentRepository tournamentRepository;
    private final DateFactory dateFactory;
    private final TournamentPagingRepository tournamentPagingRepository;
    private final PlayerRepository playerRepository;
    private final NotificationService notificationService;
    private final TeamPagingRepository teamPagingRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public TournamentAdminService(AdminRepository adminRepository, TournamentRepository tournamentRepository, DateFactory dateFactory, TournamentPagingRepository tournamentPagingRepository, PlayerRepository playerRepository, NotificationService notificationService, TeamPagingRepository teamPagingRepository, TeamRepository teamRepository) {
        this.adminRepository = adminRepository;
        this.tournamentRepository = tournamentRepository;
        this.dateFactory = dateFactory;
        this.tournamentPagingRepository = tournamentPagingRepository;
        this.playerRepository = playerRepository;
        this.notificationService = notificationService;
        this.teamPagingRepository = teamPagingRepository;
        this.teamRepository = teamRepository;
    }

    public UUID createTournament(String adminUsername, Tournament tournament){
        Admin admin = adminRepository.findById(adminUsername).orElseThrow();
        tournament.setAdmin(admin);
        tournament = tournamentRepository.save(tournament);
        return tournament.getId();
    }

    public record ModifyTournamentDTO(
            Tournament.RegistrationPeriod registrationPeriod,
            @Size(max=100) String name,
            String description,
            @Positive int maxTeamCapacity,
            Tournament.EstimatedTournamentPeriod estimatedTournamentPeriod
    ){}
    public void modifyTournament(UUID tournamentId, ModifyTournamentDTO tournamentToTakeAttributesFrom, String adminUsername){
        Tournament tournament = tournamentRepository.getTournamentById(tournamentId).orElseThrow();
        if (!tournament.getAdmin().getUsername().equals(adminUsername)){
            throw new IllegalArgumentException("You are not manager of this tournament");
        }
        tournament.modify(tournamentToTakeAttributesFrom, dateFactory.getToday());
        tournamentRepository.save(tournament);
    }


    public TournamentPageDTO getTournamentsByAdmin(String adminUsername, int page, int limit){
        return new TournamentPageDTO(
                tournamentPagingRepository.findTournamentByAdminUsername(adminUsername,PageRequest.of(page,limit)),
                tournamentRepository.countTournamentByAdminUsername(adminUsername)
        );
    }

    public void invitePlayerToClosedTournament(UUID tournamentId,Collection<String> usernames, String adminUsername){
        if( tournamentRepository.getTournamentById(tournamentId).orElseThrow() instanceof ClosedTournament ct){
            if (!ct.getAdmin().getUsername().equals(adminUsername)){
                throw new IllegalArgumentException("You are not manager of this tournament");
            }
            List<Player> players = playerRepository.findAllById(usernames);
            ct.addInvitedPlayer(players);
            players.forEach(p -> {
                notificationService.notifyPlayerOfInvite(p.getUsername());
            });
            tournamentRepository.save(ct);
        } else {
            throw new IllegalArgumentException("Not a closed tournament!");
        }
    }

    /**
     * Informs you of the teams in a tournament
     * If the registration has passed you can modify but only before you start it
     * @param teams
     * @param count
     * @param postRegistration whether the list can be changed (by deleting) only after registration is over
     * @param hasStarted whether tournament has started
     */
    public record TeamPageDTO(List<Team> teams, long count, boolean postRegistration, boolean hasStarted){}

    public TeamPageDTO getTeamsOfATournament(UUID tournamentId, String adminUsername,int page, int limit){
        var tournament = tournamentRepository.getTournamentById(tournamentId).orElseThrow();
        return new TeamPageDTO(
                teamPagingRepository.findByTournamentIdAndAdminUsername(tournamentId,adminUsername, PageRequest.of(page,limit)),
                teamRepository.countByTournamentIdAndAdminUsername(tournamentId,adminUsername),
                tournament.getRegistrationPeriod().isBefore(dateFactory.getToday()),
                tournament.hasStarted()
        );
    }

    /**
     * Don't allow if tournament not managed by this admin, registration period hasnt passed, or tournament has started
     * @param adminUsername admin username
     * @param playerUsernameToRemove username of player to remove
     * @param tournamentID id of tournament
     */
    public void kickPlayerFromTournament(String adminUsername, String playerUsernameToRemove, UUID tournamentID){
        Admin admin = adminRepository.findById(adminUsername).orElseThrow();
        Player player = playerRepository.findById(playerUsernameToRemove).orElseThrow();

        Tournament tournament = tournamentRepository.getTournamentById(tournamentID).orElseThrow();

        //TODO Checking for whether the first round has been started
        if (!tournament.getAdmin().getId().equals(adminUsername)
                || !tournament.getRegistrationPeriod().isBefore(dateFactory.getToday())
                || tournament.hasStarted()
        ){
            throw new IllegalArgumentException("Tournament not managed by this admin");
        }
        teamRepository.deleteById(new Team.TeamId(player,tournament));
    }

}
