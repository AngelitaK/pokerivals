package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, Team.TeamId> {

    @Query("Select t from Team t" +
            " where t.teamId.playerName = :username and t.teamId.tournamentId = :tournamentId")
    public Optional<Team> findByPlayerUsernameAndTournamentId(@Param("username")String Username, @Param("tournamentId") UUID tournamentId);

    @Query("select t from Team t where t.teamId.tournamentId = :tournamentId")
    public List<Team> findByTournamentId(@Param("tournamentId") UUID tournamentId);

    @Query("select COUNT(t) from Team t where t.teamId.tournamentId = :tournamentId AND t.tournament.admin.username = :adminUsername")
    public long countByTournamentIdAndAdminUsername(@Param("tournamentId") UUID tournamentId, @Param("adminUsername") String adminUsername);

}