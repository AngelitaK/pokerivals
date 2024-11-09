package com.smu.csd.pokerivals.tournament.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.time.ZonedDateTime;
import java.util.*;

@Entity
public class ClosedTournament extends Tournament {
    public ClosedTournament() {
    }

    public ClosedTournament(String name, Admin admin) {
        super(name, admin);
    }

    public ClosedTournament(String name, Admin admin, EloLimit eloLimit, RegistrationPeriod registrationPeriod) {
        super(name, admin, eloLimit, registrationPeriod);
    }

    @JsonGetter("invited_players")
    public List<String> getPlayers(){
        return invitedPlayers.stream().map(Player::getId).toList();
    }

    @ManyToMany
    @JsonIgnore
    private Set<Player> invitedPlayers = new HashSet<>();

    public void addInvitedPlayer(Collection<Player> players){
        invitedPlayers.addAll(players);
    }

    public boolean checkInvited(String username){
        for (Player p : invitedPlayers){
            if(p.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void addTeam(Team team, ZonedDateTime today){
        if(!this.invitedPlayers.contains(team.getPlayer())){
            throw new IllegalArgumentException("Player not invited!");
        }
        super.addTeam(team,today);
    }

}
