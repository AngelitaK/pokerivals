package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentPagingRepository extends PagingAndSortingRepository<Tournament, UUID> {

    @Query("select t from Tournament t where t.admin.username = :username ORDER BY t.createdAt ASC")
    List<Tournament> findTournamentByAdminUsername(@Param("username") String adminUsername, Pageable pageable);

    @Query("select t from Tournament t join t.teams ts where ts.teamId.playerName = :username ORDER BY t.createdAt ASC")
    List<Tournament> findTournamentByPlayerUsername(@Param("username") String playerUsername, Pageable pageable);

    @Query("select t from Tournament t where lower(t.name) like lower(concat('%', :query,'%')) AND t.eloLimit.minElo < :points AND :points < t.eloLimit.maxElo")
    List<Tournament> searchTournaments(@Param("query") String query,@Param("points") Double points, Pageable pageable);

    List<Tournament> findByNameLikeIgnoreCaseAndEloLimit_MinEloLessThanAndEloLimit_MaxEloLessThan(String query,Double points, Double points2, Pageable pageable);

}
