package com.smu.csd.pokerivals.match;


import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.Math.pow;

@Slf4j
@Service
public class MatchService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;

    @Autowired
    public MatchService(TournamentRepository tournamentRepository, MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public Set<Match> startSingleElimTournament(UUID uuid, ZonedDateTime today){
        Tournament tournament = tournamentRepository.getTournamentById(uuid).orElseThrow();
        if (!tournament.getMatches().isEmpty()){
            throw new IllegalArgumentException("Tournament already started");
        }

        List<Team> teams = new ArrayList<>(tournament.getTeams().stream().toList());
        // Sort by points
        teams.sort(Comparator.comparing(t -> -t.getPlayer().getPoints()));

        // find total number of rounds required
        // equal to ceil(log(n))
        int n = 1;
        int rounds = 0;
        int numTeams = teams.size();
        while (n < numTeams){
            rounds++;
            n *= 2;
        }
        int treeHeight = rounds-1;

        Match finalMatch = new Match(tournament,treeHeight);

        Set<Match> matches = finalMatch.createAndSeedTree(teams, today);
        tournament.addMatches(matches);

        return matches;
    }

    // need to put the winningTeam forward
    @Transactional
    public void advance(UUID tournamentId,int depth, int index,MatchResult matchResult, ZonedDateTime today){
//        if (matchResult.equals(MatchResult.CANCELLED)) {
//            throw new IllegalArgumentException("Cannot set match to cancelled");
//        }
        Match matchBefore = matchRepository.findById(new Match.MatchId(tournamentId,depth,index)).orElseThrow();

        // i have the team i need to move forward
        Team winningTeam = matchBefore.setMatchResult(matchResult, today);

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
     * Forfeit EITHER player!
     * @param tournamentId
     * @param depth
     * @param index
     * @param teamA if false, then team B forfeit
     * @param today
     */
    @Transactional
    public void forfeit(UUID tournamentId,int depth, int index,boolean teamA, ZonedDateTime today){

        Match matchBefore = matchRepository.findById(new Match.MatchId(tournamentId,depth,index)).orElseThrow();

        boolean bye;
        if (teamA){
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



}
