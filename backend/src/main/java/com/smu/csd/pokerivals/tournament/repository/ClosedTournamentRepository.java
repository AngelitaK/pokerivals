package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ClosedTournamentRepository extends JpaRepository<Tournament, UUID> {
    @Query("select COUNT(ct) from ClosedTournament ct join ct.invitedPlayers p where p.username = :username")
    long countClosedTournamentWherePlayerInvited(@Param("username") String adminUsername);
}
