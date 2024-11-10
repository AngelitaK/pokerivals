package com.smu.csd.pokerivals.match.repository;

import com.smu.csd.pokerivals.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPagingRepository extends JpaRepository<Match, Match.MatchId> {

}
