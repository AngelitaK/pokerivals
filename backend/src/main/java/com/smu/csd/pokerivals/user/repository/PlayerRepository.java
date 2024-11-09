package com.smu.csd.pokerivals.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.smu.csd.pokerivals.user.entity.Player;

import java.util.List;

@Component
public interface PlayerRepository extends JpaRepository<Player,String> {
    List<Player> findByUsernameContaining(String query);

    @Query("select p from Player p join p.befriendedBy bB join p.friendsWith fW " +
            "where bB.username = :username and fW.username = :username")
    List<Player> findFriendsOfPlayer(@Param("username") String username);
}
