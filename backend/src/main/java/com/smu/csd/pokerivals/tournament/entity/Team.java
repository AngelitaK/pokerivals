package com.smu.csd.pokerivals.tournament.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

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
    @JsonIgnore
    private TeamId teamId;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @ToString
    public static class TeamId implements Serializable{

        @Column(length=100)
        @Size(max=100)
        private String playerName;

        @Column(nullable = false)
        private UUID tournamentId;

        public TeamId(Player player, Tournament tournament){
            this.playerName = player.getUsername();
            this.tournamentId = tournament.getId();
        }

    }

    @ManyToOne
    @MapsId("playerName")
    @JsonIgnore
    private Player player;

    @ManyToOne
    @MapsId("tournamentId")
    @JsonIgnore
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

    public record TournamentDetails(String name, UUID id){};

    @JsonGetter("tournament")
    public TournamentDetails getTournamentDetails(){
        return new TournamentDetails(tournament.getName(),tournament.getId());
    }

    @JsonGetter("playerUsername")
    public String getPlayerUsername(){
        return teamId.getPlayerName();
    }

    @PreRemove
    public void deleteAllPokemons(){
        chosenPokemons.clear();
    }
}
