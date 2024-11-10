package com.smu.csd.pokerivals.match.repository;

import com.smu.csd.pokerivals.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, Match.MatchId> {
    List<Match> findByMatchIdTournamentId(UUID tournamentId);

//    @Query("select count(m) from Match m where m.tournament.admin.username = :username and m.timeMatchOccurs > :start and m.timeMatchOccurs < :end")
    long countByTimeMatchOccursBetweenAndTournament_Admin_Username(ZonedDateTime start, ZonedDateTime end, String username);

    @Query("select count(m) from Match m where (m.teamA.teamId.playerName = :username or m.teamB.teamId.playerName = :username) and m.timeMatchOccurs > :start and m.timeMatchOccurs < :end")
    long countByTimeMatchOccursBetweenAndTournamentPlayer(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end,  @Param("username") String username);

   long countByTimeMatchOccursBetweenOrderByTimeMatchOccursAsc( ZonedDateTime start, ZonedDateTime end);

   List<Match> findByTimeMatchOccursBetween(ZonedDateTime start, ZonedDateTime end);
}