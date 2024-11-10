package com.smu.csd.pokerivals.match;


import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.entity.MatchWrapper;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.Math.pow;

@Slf4j
@Service
public class MatchService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final DateFactory dateFactory;

    @Autowired
    public MatchService(TournamentRepository tournamentRepository, MatchRepository matchRepository, DateFactory dateFactory) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.dateFactory = dateFactory;
    }

    /**
     * Start tournament for a tournament
     * @param uuid ID of tournament that has not been started
     * @return all the matches within the tournament
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public Set<Match> startSingleEliminationTournament(UUID uuid){
        ZonedDateTime today = dateFactory.getToday();
        Tournament tournament = tournamentRepository.getTournamentById(uuid).orElseThrow();
        if (!tournament.getRegistrationPeriod().isBefore(today)){
            throw new IllegalArgumentException("Tournament registration still open");
        }
        if (!tournament.getMatches().isEmpty()){
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
    public void setMatchResult(SetMatchResultDTO dto){
        var today = dateFactory.getToday();
        Match matchBefore = matchRepository.findById(dto.matchId).orElseThrow();

        // i have the team i need to move forward
        Team winningTeam = matchBefore.setMatchResult(dto.matchResult, today);

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
            } else {
                break;
            }
            matchBefore = matchAfter;
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
    public void forfeit(ForfeitDTO dto){
        boolean forfeitTeamA = dto.forfeitTeamA;
        ZonedDateTime today = dateFactory.getToday();
        Match matchBefore = matchRepository.findById(dto.matchId).orElseThrow();
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

        if (!bye){
            return;
        }
        // have bye
        // i have the team i need to move forward
        Team winningTeam = matchBefore.finaliseBye(today);

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
            } else {
                break;
            }
        }
    }

    public List<MatchWrapper.MatchRoundDTO> generateFrontendFriendlyBrackets(UUID tournamentId){
        return MatchWrapper.generateDisplayableTreeViaBFS(MatchWrapper.reconstructTree(matchRepository.findByMatchIdTournamentId(tournamentId)));
    }



}
