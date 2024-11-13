package com.smu.csd.pokerivals.betting.dto;

import com.smu.csd.pokerivals.match.entity.Match;

public record MatchBettingSummaryDTO (
        Match match,
        long totalNoOfTeamABets,
        long totalNoOfTeamBBets,
        long totalBetsOnTeamAInCents,
        long totalBetsOnTeamBInCents
){ }
