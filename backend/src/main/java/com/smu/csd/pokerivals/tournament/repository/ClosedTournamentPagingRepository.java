package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClosedTournamentPagingRepository extends PagingAndSortingRepository<Tournament, UUID> {
    @Query("select ct from ClosedTournament ct join ct.invitedPlayers p where p.username = :username ORDER BY ct.createdAt ASC")
    List<Tournament> findClosedTournamentWherePlayerInvited(@Param("username") String adminUsername, Pageable pageable);
}
