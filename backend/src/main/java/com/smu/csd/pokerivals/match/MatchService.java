package com.smu.csd.pokerivals.match;


import com.smu.csd.pokerivals.betting.service.PaymentAsyncService;
import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.entity.MatchWrapper;
import com.smu.csd.pokerivals.match.repository.MatchPagingRepository;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.notification.dto.LambdaNotificationDTO;
import com.smu.csd.pokerivals.notification.dto.NotificationDetails;
import com.smu.csd.pokerivals.notification.dto.NotificationType;
import com.smu.csd.pokerivals.notification.service.NotificationService;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;

@Slf4j
@Service
public class MatchService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final DateFactory dateFactory;
    private final NotificationService notificationService;
    private final MatchPagingRepository matchPagingRepository;
    private final PaymentAsyncService paymentAsyncService;

    @Autowired
    public MatchService(TournamentRepository tournamentRepository, MatchRepository matchRepository, DateFactory dateFactory, NotificationService notificationService, MatchPagingRepository matchPagingRepository, PaymentAsyncService paymentAsyncService) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.dateFactory = dateFactory;
        this.notificationService = notificationService;
        this.matchPagingRepository = matchPagingRepository;
        this.paymentAsyncService = paymentAsyncService;
    }

    /**
     * Start tournament for a tournament
     * @param uuid ID of tournament that has not been started
     * @return all the matches within the tournament
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public Set<Match> startSingleEliminationTournament(UUID uuid, String adminUsername){
        ZonedDateTime today = dateFactory.getToday();
        Tournament tournament = tournamentRepository.getTournamentById(uuid).orElseThrow();
        if(!tournament.getAdminUsername().equals(adminUsername)){
            throw new IllegalArgumentException("The tournament is not managed by you");
        }
        if (!tournament.getRegistrationPeriod().isBefore(today)){
            throw new IllegalArgumentException("Tournament registration still open");
        }
        if (tournament.hasStarted()){
            throw new IllegalArgumentException("Tournament already started");
        }

        // sort in reverse order
        List<Team> teams = new ArrayList<>(tournament.getTeams().stream().toList());
        teams.sort(Comparator.comparing(t -> -t.getPlayer().getPoints()));

        // find total number of rounds required
        // equal to ceil(log(n))
        int n =1 , rounds = 0, numTeams = teams.size();
        while (n < numTeams){ rounds++; n *= 2; }
        int treeHeight = rounds-1;

        Match finalMatch = new Match(tournament,treeHeight);
        Set<Match> matches = finalMatch.createAndSeedTree(teams, today);
        tournament.addMatches(matches);

        return matches;
    }

    /**
     * DTO to set match result
     * @param matchId ID of match
     * @param matchResult result of match (reject pending)
     */
    public record SetMatchResultDTO(
            Match.MatchId matchId,
            MatchResult matchResult
    ){
        public SetMatchResultDTO {
            if (matchResult.equals(MatchResult.PENDING)){
                throw new IllegalArgumentException("Cannot set Match result to PENDING");
            }
        }

    }

    /**
     * Set who is the winner/ cancel tournament
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void setMatchResult(SetMatchResultDTO dto, String adminUsername){
        var today = dateFactory.getToday();
        Match matchBefore = matchRepository.findById(dto.matchId).orElseThrow();
        if (!matchBefore.isAdminManagingMatch(adminUsername)){
            throw new IllegalArgumentException("this tournament is not managed by you");
        }
        // i have the team i need to move forward
        Team winningTeam = matchBefore.setMatchResult(dto.matchResult, today);
        List<Player> players = new ArrayList<>();
        if (matchBefore.getTeamA() != null){
            players.add(matchBefore.getTeamA().getPlayer());
        }
        if (matchBefore.getTeamB() != null){
            players.add(matchBefore.getTeamB().getPlayer());
        }
        notificationService.pushNotificationToLambda(
                new LambdaNotificationDTO(
                        new NotificationDetails(
                                NotificationDetails.convertUsersToMap(players),
                                matchBefore.getTournament().getName(),
                                today
                        ),
                        NotificationType.MATCH_PLAYER_MATCH_OUTCOME
                )
        );
        // continue until reaching root
        while (true) {
            // if root don't attempt to move forward
            if (matchBefore.isRoot()){
                break;
            }
            // if not root node, get the next match
            int matchAfterDepth = matchBefore.getDepth()-1;
            int matchAfterIndex =  (matchBefore.getIndex() < (int) pow(2, matchBefore.getDepth()-1)) ? matchBefore.getIndex() : (int) (pow(2, matchBefore.getDepth()) - matchBefore.getIndex()-1);
            Match matchAfter = matchRepository.findById(new Match.MatchId(matchBefore.getTournamentId(),matchAfterDepth,matchAfterIndex)).orElseThrow();

            boolean bye = false;
            // insert into correct slot, checking whether this is a bye
            if (matchBefore.getIndex() < (int) pow(2 , matchBefore.getDepth() - 1)){
                bye = matchAfter.finaliseTeamA(winningTeam, today);
            } else {
                bye= matchAfter.finaliseTeamB(winningTeam, today);
            }
            // if a bye, we need to go up the chain, but get the winning team first
            if (bye){
                winningTeam = matchAfter.finaliseBye(today);
                players = new ArrayList<>();
                if (matchAfter.getTeamA() != null){
                    players.add(matchAfter.getTeamA().getPlayer());
                }
                if (matchAfter.getTeamB() != null){
                    players.add(matchAfter.getTeamB().getPlayer());
                }
                notificationService.pushNotificationToLambda(
                        new LambdaNotificationDTO(
                                new NotificationDetails(
                                        NotificationDetails.convertUsersToMap(players),
                                        matchAfter.getTournament().getName(),
                                        today
                                ),
                                NotificationType.MATCH_PLAYER_MATCH_OUTCOME
                        )
                );
            } else {
                break;
            }
            matchBefore = matchAfter;
        }
        if (dto.matchResult == MatchResult.CANCELLED) {
            paymentAsyncService.asyncProcessForfeit(dto.matchId);
        } else {
            paymentAsyncService.asyncWinBet(dto.matchId);
        }
    }

    /**
     * DTO to forfeit a team
     * @param matchId ID of match
     * @param forfeitTeamA if Team A forfeits, true o/w false
     */
    public record ForfeitDTO(
            Match.MatchId matchId,
            boolean forfeitTeamA
    ){}

    /**
     * Forfeit EITHER player! To cancel match, use advance method
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void forfeit(ForfeitDTO dto, String adminUsername){
        boolean forfeitTeamA = dto.forfeitTeamA;
        ZonedDateTime today = dateFactory.getToday();
        Match matchBefore = matchRepository.findById(dto.matchId).orElseThrow();
        if (!matchBefore.isAdminManagingMatch(adminUsername)){
            throw new IllegalArgumentException("this tournament is not managed by you");
        }
        if(matchBefore.getTimeFinalisedTeamA() == null && forfeitTeamA){
            // team A not finalised, hence cannot forfeit
            throw new IllegalArgumentException("Team A not set, hence cannot forfeit");
        }
        if(matchBefore.getTimeFinalisedTeamB() == null && !forfeitTeamA){
            // team A not finalised, hence cannot forfeit
            throw new IllegalArgumentException("Team B not set, hence cannot forfeit");
        }


        boolean bye;
        if (forfeitTeamA){
            bye = matchBefore.finaliseTeamA(null, today) ;
        } else {
            bye = matchBefore.finaliseTeamB(null, today) ;
        }
        paymentAsyncService.asyncProcessForfeit(matchBefore.getMatchId());

        if (!bye){
            return;
        }
        // have bye
        // i have the team i need to move forward
        Team winningTeam = matchBefore.finaliseBye(today);
        List<Player> players = new ArrayList<>();
        if (matchBefore.getTeamA() != null){
            players.add(matchBefore.getTeamA().getPlayer());
        }
        if (matchBefore.getTeamB() != null){
            players.add(matchBefore.getTeamB().getPlayer());
        }
        notificationService.pushNotificationToLambda(
                new LambdaNotificationDTO(
                        new NotificationDetails(
                                NotificationDetails.convertUsersToMap(players),
                                matchBefore.getTournament().getName(),
                                today
                        ),
                        NotificationType.MATCH_PLAYER_FORFEIT
                )
        );

        // continue until reaching root
        while (true) {
            // if root dont attempt to move forward
            if (matchBefore.isRoot()){
                break;
            }
            // if not root node, get the next match
            int matchAfterDepth = matchBefore.getDepth()-1;
            int matchAfterIndex =  (matchBefore.getIndex() < (int) pow(2, matchBefore.getDepth()-1)) ? matchBefore.getIndex() : (int) (pow(2, matchBefore.getDepth()) - matchBefore.getIndex()-1);
            Match matchAfter = matchRepository.findById(new Match.MatchId(matchBefore.getTournamentId(),matchAfterDepth,matchAfterIndex)).orElseThrow();

            bye = false;
            // insert into correct slot, checking whether this is a bye
            if (matchBefore.getIndex() < (int) pow(2 , matchBefore.getDepth() - 1)){
                bye = matchAfter.finaliseTeamA(winningTeam, today);
            } else {
                bye= matchAfter.finaliseTeamB(winningTeam, today);
            }

            // if a bye, we need to go up the chain, but get the winning team first
            if (bye){
                winningTeam = matchAfter.finaliseBye(today);
                players = new ArrayList<>();
                if (matchAfter.getTeamA() != null){
                    players.add(matchAfter.getTeamA().getPlayer());
                }
                if (matchAfter.getTeamB() != null){
                    players.add(matchAfter.getTeamB().getPlayer());
                }
                notificationService.pushNotificationToLambda(
                        new LambdaNotificationDTO(
                                new NotificationDetails(
                                        NotificationDetails.convertUsersToMap(players),
                                        matchAfter.getTournament().getName(),
                                        today
                                ),
                                NotificationType.MATCH_PLAYER_FORFEIT
                        )
                );
            } else {
                break;
            }
        }

    }

    public List<MatchWrapper.MatchRoundDTO> generateFrontendFriendlyBrackets(UUID tournamentId){
        return MatchWrapper.generateDisplayableTreeViaBFS(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(tournamentId)));
    }

    public record ApproveRejectMatchTimingDTO(
            Match.MatchId matchId,
            boolean approve
    ){}

    @PreAuthorize("hasAuthority('PLAYER')")
    @Transactional
    public void approveOrRejectMatchTiming(ApproveRejectMatchTimingDTO dto, String username){
        var match = matchRepository.findById(dto.matchId).orElseThrow();
        if (!match.isPlayerInMatch(username)){
            throw new IllegalArgumentException("this tournament is not participated by you");
        }
        notificationService.pushNotificationToLambda(
                new LambdaNotificationDTO(
                        new NotificationDetails(
                                NotificationDetails.convertUsersToMap(List.of(match.getTournament().getAdmin())),
                                match.getTournament().getName(),
                                dateFactory.getToday()
                        ),
                        NotificationType.MATCH_ADMIN_CHANGE_TIME_AGREEMENT
                )
        );
        // PENDING
        if (match.getTeamA() != null && match.getTeamA().getPlayer().getUsername().equals(username)){
            // Team A
            match.setTeamAAgreed(dto.approve, dateFactory.getToday());
        }
        else if (match.getTeamB().getPlayer().getUsername().equals(username)){
            // Team B
            match.setTeamBAgreed(dto.approve, dateFactory.getToday());
        }
    }

    public record SetMatchTimingDTO(
            Match.MatchId matchId,
            ZonedDateTime matchTiming
    ){}

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void setMatchTiming(String adminUsername,SetMatchTimingDTO dto){
        var match = matchRepository.findById(dto.matchId).orElseThrow();
        if (!match.isAdminManagingMatch(adminUsername)){
            throw new IllegalArgumentException("this tournament is not managed by you");
        }
        if(Objects.equals(match.getTournament().getAdmin().getUsername(), adminUsername)){
            // time validation occurs inside
            match.setTimeMatchOccursAndResetAgreement(dto.matchTiming, dateFactory.getToday());
            notificationService.pushNotificationToLambda(
                    new LambdaNotificationDTO(
                            new NotificationDetails(
                                    NotificationDetails.convertUsersToMap(List.of(match.getTeamA().getPlayer(), match.getTeamB().getPlayer())),
                                    match.getTournament().getName(),
                                    dateFactory.getToday()
                            ),
                            NotificationType.MATCH_PLAYER_TIMING_CHANGE
                    )
            );
        } else {
            throw new IllegalArgumentException("You are not admin for this match");
        }
    }

    public record MatchPageDTO(List<Match> matches, long count){}

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public MatchPageDTO getMatchesForAdminBetweenDates(String username, ZonedDateTime start, ZonedDateTime end , int page, int limit){
        return new MatchPageDTO(
                matchPagingRepository.findByTimeMatchOccursBetweenAndTournament_Admin_Username(start,end, username, PageRequest.of(page, limit)),
                matchRepository.countByTimeMatchOccursBetweenAndTournament_Admin_Username(start,end,username)
        );
    }

    @PreAuthorize("hasAuthority('PLAYER')")
    @Transactional
    public MatchPageDTO getMatchesForPlayerBetweenDates(String username, ZonedDateTime start, ZonedDateTime end , int page, int limit){
        return new MatchPageDTO(
                matchPagingRepository.findByTimeMatchOccursBetweenAndTournamentPlayer(start,end, username, PageRequest.of(page, limit)),
                matchRepository.countByTimeMatchOccursBetweenAndTournamentPlayer(start,end,username)
        );
    }

    @Transactional
    public MatchPageDTO getMatchesBetweenDates(ZonedDateTime start, ZonedDateTime end , int page, int limit){
        return new MatchPageDTO(
                matchPagingRepository.findByTimeMatchOccursBetweenOrderByTimeMatchOccursAsc(start,end, PageRequest.of(page, limit)),
                matchRepository.countByTimeMatchOccursBetweenOrderByTimeMatchOccursAsc(start,end)
        );
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void remindOfNearbyRegistrationDates(){
        var today = dateFactory.getToday();
        var matches = matchRepository.findByTimeMatchOccursBetween(
                today,today.minusMinutes(1)
        );
        for (Match m: matches){
            notificationService.pushNotificationToLambda(
                    new LambdaNotificationDTO(
                            new NotificationDetails(
                                    NotificationDetails.convertUsersToMap(Collections.singletonList(m.getTournament().getAdmin())),
                                    m.getTournament().getName(),
                                    m.getTimeMatchOccurs()
                            ),
                            NotificationType.MATCH_ADMIN_TIME_COMING
                    )
            );
            notificationService.pushNotificationToLambda(
                    new LambdaNotificationDTO(
                            new NotificationDetails(
                                    NotificationDetails.convertUsersToMap(List.of(m.getTeamA().getPlayer(), m.getTeamB().getPlayer())),
                                    m.getTournament().getName(),
                                    m.getTimeMatchOccurs()
                            ),
                            NotificationType.MATCH_PLAYER_TIME_COMING
                    )
            );
        }
    }

}
