package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Team.TeamId> {

    @Query("Select t from Team t" +
            " where t.teamId.playerName = :username and t.teamId.tournamentId = :tournamentId")
    public Optional<Team> findByPlayerUsernameAndTournamentId(@Param("username")String Username, @Param("tournamentId") String tournamentId);

}