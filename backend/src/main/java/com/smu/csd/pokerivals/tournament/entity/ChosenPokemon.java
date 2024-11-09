package com.smu.csd.pokerivals.tournament.entity;

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
import java.util.HashSet;
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
    private Pokemon pokemon;

    @ManyToOne
    @MapsId("teamId")
    private Team team;

    @ManyToMany
    private Set <Move> movesLearnt = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private POKEMON_NATURE nature;

    @ManyToOne
    private Ability ability;

    public ChosenPokemon (Team team , Pokemon pokemon){
        this.team = team;
        this.pokemon = pokemon;
        this.pokemonId = new PokemonId(team,pokemon);
    }

    public void learnMove(Move move){
        movesLearnt.add(move);
    }
    public void removeMove(Move move){
        movesLearnt.remove(move);
    }

}

