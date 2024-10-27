package com.smu.csd.pokerivals.tournament.entity;

import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Team {
    public Team(){}

    @EmbeddedId
    private TeamId teamId;

    @Override
    public boolean equals(Object other){
        if (other instanceof Team o){
            return teamId.equals(o.getTeamId());
        }
        return false;
    }
    @Embeddable
    @Getter
    @Setter
    public static class TeamId implements Serializable{
        public TeamId(){}
        @Column(nullable = false, name = "playerName", length=100)
        private String playerName;

        @Column(nullable = false, name = "tournamentId")
        private UUID tournamentId;

        public TeamId(Player player, Tournament tournament){
            this.playerName = player.getUsername();
            this.tournamentId = tournament.getId();
        }
        @Override
        public boolean equals(Object other){
            if (other instanceof TeamId o){
                return playerName.equals(o.getPlayerName()) && tournamentId.equals(o.getTournamentId());
            }
            return false;
        }

        @Override
        public int hashCode(){
            return playerName.hashCode() + tournamentId.hashCode();
        }

    }

    @ManyToOne
    @MapsId("playerName")
    private Player player;

    @ManyToOne
    @MapsId("tournamentId")
    private Tournament tournament;


    public Team (Player player, Tournament tournament , TeamId teamId) {
        this.player = player;
        this.tournament = tournament;
        this.teamId = teamId;
    }

    public Team (Player player, Tournament tournament) {
        this.player = player;
        this.tournament = tournament;
    }

    @OneToMany (mappedBy = "team")
    Set <ChosenPokemon> chosenPokemons = new HashSet<>();

    public void addPokemon(ChosenPokemon pokemon){
        chosenPokemons.add(pokemon);
    }

    public void removePokemon(ChosenPokemon pokemon){
        chosenPokemons.remove(pokemon);
    }
}
