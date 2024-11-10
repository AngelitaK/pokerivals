package com.smu.csd.pokerivals.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.smu.csd.pokerivals.user.entity.Player;

import java.util.List;

@Component
public interface PlayerRepository extends JpaRepository<Player,String> {
    List<Player> findByUsernameContainingIgnoreCase(String query);

    @Query("select COUNT(p) from Player p join p.befriendedBy bB join p.friendsWith fW " +
            "where bB.username = :username and fW.username = :username ORDER BY p.points DESC")
    long countFriendsOfPlayer(@Param("username") String username);

    @Query("SELECT COUNT(x) FROM Player x WHERE x not in (select p from Player p join p.befriendedBy bB join p.friendsWith fW " +
            "where bB.username = :username and fW.username = :username)")
    long countNotFriendsOfPlayer(@Param("username") String username);

    long countByUsernameContainingIgnoreCase(String query);

    long countByClan_Name(String clanName);
}
