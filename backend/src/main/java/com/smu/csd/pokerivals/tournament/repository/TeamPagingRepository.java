package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Repository
public interface TeamPagingRepository extends PagingAndSortingRepository<Tournament, UUID> {

    @Query("select t from Team t where t.teamId.tournamentId = :tournamentId AND t.tournament.admin.username = :adminUsername")
    public List<Team> findByTournamentIdAndAdminUsername(@Param("tournamentId") UUID tournamentId, @Param("adminUsername") String adminUsername, Pageable pageable);

}
