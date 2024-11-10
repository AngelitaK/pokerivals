package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.entity.PlaceBetTransaction;
import com.smu.csd.pokerivals.betting.entity.Transaction;
import com.smu.csd.pokerivals.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceBetTransactionRepository extends JpaRepository<PlaceBetTransaction, Transaction.TransactionID> {
    long countByChangeInCentsAndPlayer_Username(long amount, String username);
    long countByChangeInCentsLessThanAndPlayer_Username(long amount, String username);

    @Query("""
            select new com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO(
                t.match,
                SUM(CASE WHEN t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_A THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_B THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_A THEN t.betAmount ELSE 0 END),
                SUM(CASE WHEN t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_B THEN t.betAmount ELSE 0 END)
            ) from PlaceBetTransaction t
            where
                t.match.matchId = :matchId
            group by t.match
            """)
    Optional<MatchBettingSummaryDTO> getBettingSummaryForOneMatch(@Param("matchId") Match.MatchId matchId);


    @Query("""
            SELECT COALESCE(COUNT(distinct(m.matchId)), 0)
            FROM PlaceBetTransaction t JOIN t.match m
            WHERE
                m.timeMatchOccurs > :today AND
                m.matchResult = com.smu.csd.pokerivals.match.entity.MatchResult.PENDING AND
                m.forfeitedTeam IS NULL

            """)
    long countBettingSummaryForFutureMatches(@Param("today") ZonedDateTime today);

    List<PlaceBetTransaction> findByMatch_MatchId(Match.MatchId matchId);
}
