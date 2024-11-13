package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.dto.CompletedMatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.entity.PlaceBetTransaction;
import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface PlaceBetTransactionPagingRepository extends PagingAndSortingRepository<PlaceBetTransaction, Transaction.TransactionID> {
    List<Transaction> findByChangeInCentsAndPlayer_Username(long amount, String username, Pageable pageable);
    List<Transaction> findByChangeInCentsLessThanAndPlayer_Username(long amount, String username, Pageable pageable);

    @Query("""
            select new com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO(
                m,
                SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_A) THEN 1 ELSE 0 END),
                SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_B) THEN 1 ELSE 0 END),
                SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_A) THEN t.betAmount ELSE 0 END),
                SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_B) THEN t.betAmount ELSE 0 END)
            ) from PlaceBetTransaction t JOIN t.match m
            where
                m.timeMatchOccurs > :today AND
                m.matchResult = com.smu.csd.pokerivals.match.entity.MatchResult.PENDING AND
                m.forfeitedTeam is null
            group by m
            order by m.timeMatchOccurs ASC
            """)
    List<MatchBettingSummaryDTO> getBettingSummaryForFutureMatches(@Param("today") ZonedDateTime today, Pageable pageable);

    @Query("""
            select new com.smu.csd.pokerivals.betting.dto.CompletedMatchBettingSummaryDTO(
            t.match,
            SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_A) THEN 1 ELSE 0 END),
            SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_B) THEN 1 ELSE 0 END),
            SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_A) THEN COALESCE(t.betAmount, 0) ELSE 0 END),
            SUM(CASE WHEN (t.side = com.smu.csd.pokerivals.betting.entity.BettingSide.TEAM_B) THEN COALESCE(t.betAmount, 0) ELSE 0 END),
            SUM(COALESCE(wt.changeInCents, 0))
            ) from PlaceBetTransaction t LEFT JOIN t.winBetTransaction wt
            where
                t.match.matchResult <> com.smu.csd.pokerivals.match.entity.MatchResult.PENDING AND
                t.match.matchResult <> com.smu.csd.pokerivals.match.entity.MatchResult.CANCELLED AND
                t.match.forfeitedTeam is null
            group by t.match, t.match.matchResultRecordedAt
            order by t.match.matchResultRecordedAt ASC
            """)
    List<CompletedMatchBettingSummaryDTO> getBettingSummaryForPastMatches(Pageable pageable);

}
