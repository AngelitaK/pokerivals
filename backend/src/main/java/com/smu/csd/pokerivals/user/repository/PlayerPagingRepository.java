package com.smu.csd.pokerivals.user.repository;

import com.smu.csd.pokerivals.user.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerPagingRepository extends PagingAndSortingRepository<Player, String> {
    @Query("select p from Player p join p.befriendedBy bB join p.friendsWith fW " +
            "where bB.username = :username and fW.username = :username")
    List<Player> findFriendsOfPlayer(@Param("username") String username, Pageable pageable);

    @Query("SELECT x FROM Player x WHERE  x not in (select p from Player p join p.befriendedBy bB join p.friendsWith fW " +
            "where bB.username = :username and fW.username = :username)")
    List<Player> findNotFriendsOfPlayer(@Param("username") String username, Pageable pageable);

    List<Player> findByUsernameContainingIgnoreCase(String query, Pageable pageable);

    List<Player> findByClan_NameIgnoreCaseOrderByPointsDesc(String clanName, Pageable pageable);
}
