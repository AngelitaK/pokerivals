package com.smu.csd.pokerivals.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.smu.csd.pokerivals.security.authentication.IncompleteGoogleAuthentication;
import com.smu.csd.pokerivals.user.entity.Clan;
import com.smu.csd.pokerivals.user.entity.ClanRepository;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final GoogleIdTokenVerifier verifier;
    private final ClanRepository clanRepository;

    @Autowired
    public PlayerService(PlayerRepository repo, GoogleIdTokenVerifier verifier, ClanRepository clanRepository){
        this.playerRepository= repo;
        this.verifier = verifier;
        this.clanRepository = clanRepository;
    }

    /**
     * Not transactional
     *
     * @param player new user to be registered
     * @param token Authentication containing token sent by the new Player
     */
    @SneakyThrows
    public void register(Player player, IncompleteGoogleAuthentication token){
            GoogleIdToken idToken = verifier.verify((String) token.getCredentials());
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.getSubject();
                log.trace("Verified as" + payload.get("name"));

                player.updateGoogleSub(new Date(System.currentTimeMillis()),userId);
                player.setEmail((String) payload.get("email"));
                player = playerRepository.save(player);

            } else {
                log.info("Expired token");
                throw new BadCredentialsException("Expired token");
            }
    }

    @RolesAllowed("ROLE_PLAYER")
    public List<Player> getFriendsOf(String username){
        return playerRepository.findFriendsOfPlayer(username);
    }

    /**
     * Make two players friends
     * @param username1 First player
     * @param username2 Second Player
     */
    @Transactional
    @RolesAllowed("ROLE_PLAYER")
    public void connectAsFriends(String username1, String username2){
        var player1 = playerRepository.findById(username1).orElseThrow();
        var player2 = playerRepository.findById(username2).orElseThrow();
        player1.addFriend(player2);
        playerRepository.save(player1);
        playerRepository.save(player2);
    }

    /**
     * Make two players not friends
     * @param username1 First player
     * @param username2 Second Player
     */
    @Transactional
    @RolesAllowed("ROLE_PLAYER")
    public void disconnectAsFriends(String username1, String username2){
        var player1 = playerRepository.findById(username1).orElseThrow();
        var player2 = playerRepository.findById(username2).orElseThrow();
        player1.removeFriend(player2);
        playerRepository.save(player1);
        playerRepository.save(player2);
    }

    public List<Player> searchPlayersByUsername(String query){
        return playerRepository.findByUsernameContaining(query);
    }

    public Player getUser(String username){
        return playerRepository.findById(username).orElseThrow();
    }

    @Transactional
    public void addToClan(String username, String clanName){
        Player player = playerRepository.findById(username).orElseThrow();
        Clan clan = clanRepository.findById(clanName).orElseThrow();

        player.addToClan(clan);
        playerRepository.save(player);
        clanRepository.save(clan);
    }


}
