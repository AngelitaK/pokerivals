package com.smu.csd.pokerivals.match.repository;

import com.smu.csd.pokerivals.match.entity.Match;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface MatchPagingRepository extends JpaRepository<Match, Match.MatchId> {
 //  @Query("select m from Match m where m.tournament.admin.username = :username and m.timeMatchOccurs > :start and m.timeMatchOccurs < :end")
   List<Match> findByTimeMatchOccursBetweenAndTournament_Admin_Username( ZonedDateTime start, ZonedDateTime end,  String username, Pageable pageable);

    List<Match> findByTimeMatchOccursBetweenOrderByTimeMatchOccursAsc( ZonedDateTime start, ZonedDateTime end, Pageable pageable);

   @Query("select m from Match m where (m.teamA.teamId.playerName = :username or m.teamB.teamId.playerName = :username) and m.timeMatchOccurs > :start and m.timeMatchOccurs < :end")
   List<Match> findByTimeMatchOccursBetweenAndTournamentPlayer(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end, @Param("username") String username, Pageable pageable);
}
