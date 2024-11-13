package com.smu.csd.pokerivals.tournament.dto;

import com.smu.csd.pokerivals.tournament.entity.Tournament;

import java.util.List;

public record TournamentPageDTO(
        List<Tournament> tournaments,
        long count
){}
