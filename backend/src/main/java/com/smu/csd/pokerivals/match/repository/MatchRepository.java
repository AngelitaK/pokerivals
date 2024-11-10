package com.smu.csd.pokerivals.match.repository;

import com.smu.csd.pokerivals.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, Match.MatchId> {
    List<Match> findByMatchIdTournamentId(UUID tournamentId);
}