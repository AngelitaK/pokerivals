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
}
