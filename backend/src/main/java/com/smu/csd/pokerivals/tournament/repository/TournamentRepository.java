package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    Optional<Tournament> getTournamentByName(String name);

    Optional<Tournament> getTournamentById(UUID id);

    @Query("select t from Tournament t where t.admin.username = :username")
    List<Tournament> findTournamentByAdminUsername(@Param("username") String adminUsername);

    @Query("select count(t) from Tournament t where t.admin.username = :username")
    long countTournamentByAdminUsername(@Param("username") String adminUsername);

    @Query("select t from Tournament t join t.teams ts where ts.teamId.playerName = :username ORDER BY t.createdAt ASC")
    List<Tournament> findTournamentByPlayerUsername(@Param("username") String adminUsername);

    @Query("select count(t) from Tournament t join t.teams ts where ts.teamId.playerName = :username ORDER BY t.createdAt ASC")
    long countTournamentByPlayerUsername(@Param("username") String adminUsername);

    @Query("select count(t) from Tournament t where upper(t.name) like lower(concat('%', :query,'%')) AND t.eloLimit.minElo < :points AND :points < t.eloLimit.maxElo")
    long countSearchResult(@Param("query") String query,@Param("points") double points);
}
