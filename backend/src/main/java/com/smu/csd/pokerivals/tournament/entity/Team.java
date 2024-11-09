package com.smu.csd.pokerivals.tournament.entity;

import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Team {
    public Team(){}

    @EmbeddedId
    @EqualsAndHashCode.Include
    private TeamId teamId;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class TeamId implements Serializable{

        @Column(nullable = false, name = "playerName", length=100)
        private String playerName;

        @Column(nullable = false, name = "tournamentId")
        private UUID tournamentId;

        public TeamId(Player player, Tournament tournament){
            this.playerName = player.getUsername();
            this.tournamentId = tournament.getId();
        }

    }

    @ManyToOne
    @MapsId("playerName")
    private Player player;

    @ManyToOne
    @MapsId("tournamentId")
    private Tournament tournament;


    public Team (Player player, Tournament tournament) {
        this.player = player;
        this.tournament = tournament;
        this.teamId = new TeamId(player,tournament);
    }

    @Size(min=1, max=6)
    @OneToMany (mappedBy = "team", orphanRemoval = true, cascade = CascadeType.ALL)
    Set <ChosenPokemon> chosenPokemons = new HashSet<>();

    public void addChosenPokemon(ChosenPokemon pokemon){
        chosenPokemons.add(pokemon);
    }
    public void removeChosenPokemon(ChosenPokemon pokemon){
        chosenPokemons.remove(pokemon);
    }
}
