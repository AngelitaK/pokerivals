package com.smu.csd.pokerivals.match.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.Math.round;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "battle")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match {

    @Override
    public String toString() {
        Team teamA;
        Team teamB;
        if (forfeitedTeam != null){
            teamA = (this.teamA == null) ? forfeitedTeam : this.teamA;
            teamB = (this.teamB == null) ? forfeitedTeam : this.teamB;
        } else {
            teamA = this.teamA;
            teamB = this.teamB;
        }
        return "Match{" +
                (teamA == null ? "EMPTY" : teamA.getPlayer().getUsername())
                +" (A)'s points=" + (teamA == null ? "EMPTY" : round(teamA.getPlayer().getPoints())) +
                ", "+
                (teamB == null ? "EMPTY" : teamB.getPlayer().getUsername())
                +" (B)'s points=" + (teamB == null ? "EMPTY" : round(teamB.getPlayer().getPoints())) +
                ", depth=" + getDepth() +
                ", idx=" + getIndex() +
                ", final=" + (timeFinalisedTeamA == null ? "" : "A") +  (timeFinalisedTeamB == null ? "" : "B")+
                ", result=" + matchResult +
                ", forfeit=" + isForfeited() +
                '}';
    }

    @EmbeddedId
    @JsonIgnore
    @EqualsAndHashCode.Include
    private MatchId matchId;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString(onlyExplicitlyIncluded = true)
    public static class MatchId implements Serializable {
        @Column(name = "tournament_id" , insertable=false, updatable=false)
        private UUID tournamentId;

        /**
         * Depth of final match is 0, depth of first round is maxNoOfRounds-1
         */
        @ToString.Include
        private int depth;

        /**
         * Index of two sibling nodes of depth d must be summed up to be 2^d -1
         * Node that gives team A to parent has same index as parent
         * Node that gives team B to parent has index = 2^d -1 - parent's node
         */
        @ToString.Include
        @Column(name = "idx")
        private int index;
    }

    /**
     * Effectively the height of tree (based on number of edges not vertices)
     */
    @JsonIgnore
    private int treeHeight;

    /**
     * Round is depth but counting backwards
     * @return which round this match belongs to
     */
    @JsonIgnore
    public int getRound(){
        return treeHeight - getDepth();
    }

    @MapsId("tournamentId")
    @ManyToOne
    @JsonIgnore
    protected Tournament tournament;

    @JsonGetter("index")
    public int getIndex(){
        return matchId.getIndex();
    };

    public int getDepth(){
        return matchId.getDepth();
    }

    @JsonGetter("tournament_id")
    public UUID getTournamentId(){
        return matchId.getTournamentId();
    };

    @JsonIgnore
    public boolean isLeaf(){ return  getRound() == 0;}

    @JsonIgnore
    public boolean isRoot(){ return  matchId.getDepth() == 0; }

    @JsonIgnore
    @ManyToOne
    @Setter(AccessLevel.NONE)
    private Team teamA;

    @JsonGetter("teams")
    private List<MatchWrapper.TeamInMatchDTO> getTeams(){
        Team teamA;
        Team teamB;
        if (forfeitedTeam != null){
            teamA = (this.teamA == null) ? forfeitedTeam : this.teamA;
            teamB = (this.teamB == null) ? forfeitedTeam : this.teamB;
        } else {
            teamA = this.teamA;
            teamB = this.teamB;
        }
        return new ArrayList<>(
                List.of(
                        new MatchWrapper.TeamInMatchDTO(
                                (teamA == null ? "" : teamA.getPlayerUsername()),
                                (teamA == null ? 0.0 : teamA.getPlayer().getPoints()),
                                teamA == null
                        ),
                        new MatchWrapper.TeamInMatchDTO(
                                (teamB == null ? "" : teamB.getPlayerUsername()),
                                (teamB == null ? 0.0 : teamB.getPlayer().getPoints()),
                                teamB == null
                        )
                )
        );
    }

    @JsonGetter
    public boolean isForfeited(){
        return forfeitedTeam != null;
    }

    @Setter(AccessLevel.NONE)
    private ZonedDateTime timeFinalisedTeamA;

    @Setter(AccessLevel.NONE)
    private double changeInPointsTeamA;
    /**
     * Not only set team but mark down the time
     *      * If set to null but the team exists, this is forfeit, record as such
     * @param team may be null indicating previous player forfeit
     * @param today today's date
     * @return whether a Bye occurred
     */
    public boolean finaliseTeamA(Team team, ZonedDateTime today){
        if (teamA != null && team != null) {
            throw new IllegalArgumentException("You are not allowed to overwrite the team");
        } else if (teamA != null){
            // Forfeit
            forfeitedTeam = teamA;
        }
        this.teamA = team;
        timeFinalisedTeamA = today;
        // if  [both teams are finalise] and [either is null]
        return (timeFinalisedTeamA != null && timeFinalisedTeamB != null) && (teamA == null || teamB == null);
    }

    @JsonIgnore
    @ManyToOne
    @Setter(AccessLevel.NONE)
    private Team forfeitedTeam;

    @ManyToOne
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private Team teamB;


    @Setter(AccessLevel.NONE)
    private ZonedDateTime timeFinalisedTeamB;

    @Setter(AccessLevel.NONE)
    private double changeInPointsTeamB;

    /**
     * Not only set team but mark down the time
     * If set to null but the team exists, this is forfeit, record as such
     * @param team may be null indicate no player/forfeit
     * @param today today's date
     * @return whether a Bye occurred
     */
    public boolean finaliseTeamB(Team team, ZonedDateTime today){
        if (teamB != null && team != null) {
            throw new IllegalArgumentException("You are not allowed to overwrite the team");
        } else if (teamB != null){
            // Forfeit
            forfeitedTeam = teamB;
        }
        this.teamB = team;
        timeFinalisedTeamB = today;
        // if  [both teams are finalise] and [either is null]
        return (timeFinalisedTeamA != null && timeFinalisedTeamB != null) && (teamA == null || teamB == null);
    }

    @Setter(AccessLevel.NONE)
    private MatchResult matchResult = MatchResult.PENDING;

    /**
     * Mainly used to create root node
     * @param tournament tournament
     * @param treeHeight max tree height i.e. number of rounds -1
     * @param index index of this match
     * @param depth depth of this match (final match has depth of 0)
     */
    public Match (Tournament tournament , int depth , int index, int treeHeight){
        this.tournament = tournament;
        this.matchId = new MatchId(tournament.getId() ,depth,index);
        this.treeHeight = treeHeight;
    }

    /**
     * "Duplicate the callingMatch" - the new match will be same tournament, tree height
     * @param callingMatch parent match of this match - ONLY READ NOT WRITTEN TO
     * @param isMatchBeforeA if true, this will be callingMatch's before A o/w is before B
     */
    private Match (Match callingMatch, boolean isMatchBeforeA){
        this.tournament = callingMatch.tournament;
        int index = isMatchBeforeA ? callingMatch.getIndex() : (int)Math.pow(2, callingMatch.matchId.getDepth() + 1) - 1 - callingMatch.matchId.getIndex();
        this.matchId = new MatchId(callingMatch.tournament.getId(),callingMatch.matchId.depth+1,index);
        this.treeHeight= callingMatch.getTreeHeight();
    }

    /**
     * Given a match where Player B has x points and Player A has y points
     *
     * @param pointsDifference x - y as described above
     * @return win rate of player A
     */
    private static double getWinRate(double pointsDifference){
        return 1 / (1+ Math.pow(10, (pointsDifference/400)));
    }

    /**
     * Create a tree where the tree this is called upon is the root (must be depth 0)
     * @param teams teams to seed this tree with
     * @param today date today (for injection)
     * @return all the match object in the tree
     */
    public Set<Match> createAndSeedTree(List<Team> teams, ZonedDateTime today){
        if(!isRoot()){
            throw new IllegalArgumentException("cannot create tree from non-root node");
        }
        Set<Match> result = new HashSet<>();
        createAndSeedTreeRecursive(result, teams, today);
        return result;
    }

    /**
     * Recursively create a tree, edge case is leaf nodes - here you seed from list of teams
     * @return whether THIS is a BYE match
     */
    private boolean createAndSeedTreeRecursive(Set<Match> result, List<Team> teams, ZonedDateTime today){
        result.add(this);
        // if i am at a leaf
        if (this.isLeaf()){
            // no check - impossible as both cant be finalised
            int lowerIndex = this.getIndex();
            this.finaliseTeamA(teams.get(lowerIndex), today);

            // higher index
            int higherIndex = (int) Math.pow(2 , this.getMatchId().getDepth() + 1) - 1 - lowerIndex;

            return finaliseTeamB(higherIndex >= teams.size() ? null : teams.get(higherIndex), today);
        }
        // i am not at leaf - create my children
        Match matchBeforeA = new Match(this,true);
        Match matchBeforeB = new Match(this,false);

        // ask them to reproduce and if they are bye, take the bye team and i keep
        var matchBeforeAGotBye = matchBeforeA.createAndSeedTreeRecursive(result, teams, today);
        if (matchBeforeAGotBye){
            // no check - impossible as both cant be finalised
            finaliseTeamA(matchBeforeA.finaliseBye(today),today);
        }
        var matchBeforeBGotBye = matchBeforeB.createAndSeedTreeRecursive( result, teams,today);
        if (matchBeforeBGotBye){
            // i may become bye so i need to tell my parent
            return finaliseTeamB(matchBeforeB.finaliseBye(today),today);
        }
        //  only A got finalised (impossible to be bye) or both got people
        return false;
    }

    public  Match(Tournament tournament,int treeHeight){
        this(tournament,0,0,treeHeight);
    }

    @Setter(AccessLevel.NONE)
    private ZonedDateTime matchResultRecordedAt;

    /**
     * Set the match result of this match
     * @param matchResult result of this match (cannot be pending)
     * @param today today date
     * @return the "winning" team (maybe null! in the case of double forfeit)
     */
    public Team setMatchResult(MatchResult matchResult, ZonedDateTime today) {
        if(matchResultRecordedAt != null){
            throw new IllegalArgumentException("Match result already recorded");
        }
        if (timeFinalisedTeamA == null || timeFinalisedTeamB == null ){
            throw new IllegalArgumentException("both teams not finalised!");
        }
        if (matchResult.equals(MatchResult.PENDING)){
            throw new IllegalArgumentException("cannot set match result to pending");
        }

        this.matchResult = matchResult;
        this.matchResultRecordedAt = today;

        // if both are not null
        if (teamA != null && teamB != null) {
            if (matchResult.equals(MatchResult.CANCELLED)){ return null; }

            double differenceBetweenBAndA = teamB.getPlayer().getPoints() - teamA.getPlayer().getPoints();
            // calculate both side win rate
            double teamAWinRate = getWinRate(differenceBetweenBAndA);
            double teamBWinRate = 1 - teamAWinRate;

            // change elo based on who won
            return switch (matchResult) {
                case TEAM_A -> {
                    double teamAInitialPoints = teamA.getPlayer().getPoints();
                    double teamBInitialPoints = teamB.getPlayer().getPoints();

                    teamA.getPlayer().changeElo(teamAWinRate, true);
                    teamB.getPlayer().changeElo(teamBWinRate, false);

                    changeInPointsTeamA = teamA.getPlayer().getPoints() - teamAInitialPoints;
                    changeInPointsTeamB = teamB.getPlayer().getPoints() - teamBInitialPoints;

                    yield teamA;
                }
                case TEAM_B -> {
                    double teamAInitialPoints = teamA.getPlayer().getPoints();
                    double teamBInitialPoints = teamB.getPlayer().getPoints();

                    teamB.getPlayer().changeElo(teamAWinRate, true);
                    teamA.getPlayer().changeElo(teamBWinRate, false);

                    changeInPointsTeamA = teamA.getPlayer().getPoints() - teamAInitialPoints;
                    changeInPointsTeamB = teamB.getPlayer().getPoints() - teamBInitialPoints;

                    yield teamB;
                }
                default -> {
                    // pending already accounted for above
                    // cancelled already results in immediate return
                    throw new IllegalArgumentException("cannot set match result to PENDING/CANCELLED when both present");
                }
            };
            // any of them is null (not both null)
        } else {
            return switch (matchResult){
                case CANCELLED ->{yield null;}
                case TEAM_A -> {yield teamA;}
                case TEAM_B -> {yield teamB;}
                default -> {
                    throw new IllegalArgumentException("cannot set match result to PENDING");
                }
            };
        }
    }
    /**
     * For a match, if both are finalised, return the player (OR NULL) that got Bye
     * Throws illegal argument exception if both players forfeited (null)
     */
    public Team finaliseBye(ZonedDateTime today){
        if (timeFinalisedTeamA == null || timeFinalisedTeamB == null){
            throw new IllegalArgumentException("Match teams are not finalised");
        }

        // both are null
        if (teamA == null && teamB == null ){
            return setMatchResult(MatchResult.CANCELLED, today);
        }
        // A is null, B default win
        if (teamA == null){
            return setMatchResult(MatchResult.TEAM_B, today);
        }
        // B is null, A default win
        return  setMatchResult(MatchResult.TEAM_A, today);
    }

    // timing agreement stuff
    @Setter(AccessLevel.NONE)
    private ZonedDateTime timeMatchOccurs;
    @Setter(AccessLevel.NONE)
    private ZonedDateTime timeMatchOccursSetOn;


    public void setTimeMatchOccursAndResetAgreement(ZonedDateTime eventTime, ZonedDateTime today){
        if (!matchResult.equals(MatchResult.PENDING)){
            throw new IllegalArgumentException("Match has already concluded");
        }
        if (!eventTime.isAfter(today)){
            throw new IllegalArgumentException("eventTime is not after today");
        };

        timeMatchOccurs = eventTime;
        timeMatchOccursSetOn = today;

        timeTeamARespondedTiming = null;
        timeTeamBRespondedTiming = null;
        teamAAgreed = false;
        teamBAgreed = false;
    }

    @Setter(AccessLevel.NONE)
    private ZonedDateTime timeTeamARespondedTiming;
    @Setter(AccessLevel.NONE)
    private ZonedDateTime timeTeamBRespondedTiming;
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private boolean teamAAgreed;
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private boolean teamBAgreed;

    public void setTeamAAgreed(boolean agree, ZonedDateTime today){
        if (getBothTeamAgreementStatus().equals(TeamAgreementStatus.APPROVED)){
            throw new IllegalArgumentException("Tournament date already agreed upon!");
        }
        if (getBothTeamAgreementStatus().equals(TeamAgreementStatus.REJECTED)){
            throw new IllegalArgumentException("Timing is rejected already");
        }
        teamAAgreed = agree;
        timeTeamARespondedTiming = today;
    }
    public void setTeamBAgreed(boolean agree, ZonedDateTime today){
        if (getBothTeamAgreementStatus().equals(TeamAgreementStatus.APPROVED)){
            throw new IllegalArgumentException("Tournament date already agreed upon!");
        }
        if (getBothTeamAgreementStatus().equals(TeamAgreementStatus.REJECTED)){
            throw new IllegalArgumentException("Timing is rejected already");
        }
        teamBAgreed = agree;
        timeTeamBRespondedTiming = today;
    }

    @JsonGetter("team_a_agree_timing")
    public TeamAgreementStatus getTeamAAgreementStatus(){
        if (timeTeamARespondedTiming == null){
            return TeamAgreementStatus.PENDING;
        }
        return teamAAgreed ? TeamAgreementStatus.APPROVED : TeamAgreementStatus.REJECTED;
    }

    @JsonGetter("team_b_agree_timing")
    public TeamAgreementStatus getTeamBAgreementStatus(){
        if (timeTeamBRespondedTiming == null){
            return TeamAgreementStatus.PENDING;
        }
        return teamBAgreed ? TeamAgreementStatus.APPROVED : TeamAgreementStatus.REJECTED;
    }

    @JsonGetter("both_agree_timing")
    public TeamAgreementStatus getBothTeamAgreementStatus(){
        TeamAgreementStatus teamAAgreementStatus = getTeamAAgreementStatus();
        TeamAgreementStatus teamBAgreementStatus = getTeamBAgreementStatus();

        if (teamAAgreementStatus.equals(TeamAgreementStatus.APPROVED)
                && teamBAgreementStatus.equals(teamAAgreementStatus)){
            // BOTH APPROVED
            return TeamAgreementStatus.APPROVED;
        } else if (teamBAgreementStatus.equals(TeamAgreementStatus.REJECTED)
                || teamAAgreementStatus.equals(TeamAgreementStatus.REJECTED)){
            // EITHER rejected
            return TeamAgreementStatus.REJECTED;
        } else {
            // EITHER both pending or either approved
            return TeamAgreementStatus.PENDING;
        }
    }

    @JsonGetter("can_set_result")
    private boolean canSetMatchResult(){
        return timeFinalisedTeamB != null && timeFinalisedTeamA != null && matchResult.equals(MatchResult.PENDING);
    }

    @JsonIgnore
    public boolean isPlayerInMatch(String playerUsername){
        return teamA.getPlayer().getUsername().equals(playerUsername) || teamB.getPlayer().getUsername().equals(playerUsername);
    }

    @JsonIgnore
    public boolean isAdminManagingMatch(String adminUsername){
        return tournament.getAdminUsername().equals(adminUsername);
    }
}

