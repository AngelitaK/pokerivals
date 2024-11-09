package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    Optional<Tournament> getTournamentByName(String name);

    Optional<Tournament> getTournamentById(UUID id);
}
