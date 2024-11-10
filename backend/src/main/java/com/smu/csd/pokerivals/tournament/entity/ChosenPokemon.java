package com.smu.csd.pokerivals.tournament.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smu.csd.pokerivals.pokemon.entity.Ability;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.POKEMON_NATURE;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "CHOSEN_POKEMON")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class ChosenPokemon {

    @EmbeddedId
    @EqualsAndHashCode.Include
    @JsonIgnore
    private PokemonId pokemonId;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PokemonId implements Serializable{
        @Column(nullable = false, name = "pokemonId")
        private Integer pokemonId;

        @Column(nullable = false, name = "teamId")
        private Team.TeamId teamId;

        public PokemonId(Team team, Pokemon pokemon) {
            this.teamId = team.getTeamId();
            this.pokemonId = pokemon.getId();
        }
    }

    @ManyToOne
    @MapsId("pokemonId")
    @JsonIgnore
    private Pokemon pokemon;

    public record PokemonDetails(String name, int id, Pokemon.Stats stats){}

    @JsonGetter("pokemon")
    public PokemonDetails getPokemonDetails(){
        return new PokemonDetails(pokemon.getName(), pokemonId.getPokemonId(), pokemon.getStats());
    }

    @ManyToOne
    @MapsId("teamId")
    @JsonIgnore
    private Team team;


    @ManyToMany
    @JsonIgnore
    private Set <Move> movesLearnt = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private POKEMON_NATURE nature;

    @ManyToOne
    @JsonIgnore
    private Ability ability;

    public ChosenPokemon (Team team , Pokemon pokemon){
        this.team = team;
        this.pokemon = pokemon;
        this.pokemonId = new PokemonId(team,pokemon);
    }

    @JsonGetter("moves")
    private List<String> getMovesAsString(){
        return new ArrayList<>(movesLearnt.stream().map(Move::getName).toList());
    }

    @JsonGetter("ability")
    private String getAbilityAsString(){
        return ability.getName();
    }

    public void learnMove(Move move){
        movesLearnt.add(move);
    }
    public void removeMove(Move move){
        movesLearnt.remove(move);
    }

}

