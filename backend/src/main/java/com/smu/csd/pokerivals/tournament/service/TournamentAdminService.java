package com.smu.csd.pokerivals.tournament.service;


import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.notification.dto.LambdaNotificationDTO;
import com.smu.csd.pokerivals.notification.dto.NotificationDetails;
import com.smu.csd.pokerivals.notification.dto.NotificationType;
import com.smu.csd.pokerivals.notification.service.NotificationService;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
            notificationService.pushNotificationToLambda(new LambdaNotificationDTO(
                    new NotificationDetails(
                            NotificationDetails.convertUsersToMap(players),
                            ct.getName(),
                            dateFactory.getToday()
                    ),
                    NotificationType.TOURNAMENT_PLAYER_INVITE
            ));
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

        if (!tournament.getAdmin().getId().equals(adminUsername)
                || !tournament.getRegistrationPeriod().isBefore(dateFactory.getToday())
                || tournament.hasStarted()
        ){
            throw new IllegalArgumentException("Tournament not managed by this admin");
        }
        teamRepository.deleteById(new Team.TeamId(player,tournament));
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void remindOfNearbyRegistrationDates(){
        var today = dateFactory.getToday();
        var tournaments = tournamentRepository.findByRegistrationPeriod_RegistrationEndBetween(
                today.minusMinutes(1),today
        );
        for (Tournament t: tournaments){
            notificationService.pushNotificationToLambda(
                    new LambdaNotificationDTO(
                            new NotificationDetails(
                                    NotificationDetails.convertUsersToMap(Collections.singletonList(t.getAdmin())),
                                    t.getName(),
                                    t.getRegistrationPeriod().getRegistrationEnd()
                            ),
                            NotificationType.TOURNAMENT_ADMIN_REGISTRATION_CLOSED
                    )
            );
        }
    }

}
