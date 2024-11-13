package com.smu.csd.pokerivals.tournament.entity;

import com.smu.csd.pokerivals.pokemon.entity.Ability;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.POKEMON_NATURE;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "CHOSEN_POKEMON")
public class ChosenPokemon {
    public ChosenPokemon(){}
    @EmbeddedId
    private PokemonId pokemonId;
    @Override
    public boolean equals(Object other){
        if (other instanceof ChosenPokemon o){
            return pokemonId.equals(o.getPokemonId());
        }
        return false;
    }
    @Embeddable
    @Getter
    @Setter
    public static class PokemonId implements Serializable{
        public PokemonId(){}
        @Column(nullable = false, name = "pokemonId")
        private Integer pokemonId;

        @Column(nullable = false, name = "teamId")
        private Team.TeamId teamId;

        @Override
        public boolean equals(Object other){
            if (other instanceof PokemonId o){
                return pokemonId.equals(o.getPokemonId()) && teamId.equals(o.getTeamId());
            }
            return false;
        }

        @Override
        public int hashCode(){
            return pokemonId.hashCode() + teamId.hashCode();
        }
        public PokemonId (Pokemon pokemon , Team team){
            this.pokemonId = pokemon.getId();
            this.teamId = team.getTeamId();
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
    private POKEMON_NATURE nature;

    @ManyToOne
    private Ability ability;

    public ChosenPokemon (Team team , Pokemon pokemon , PokemonId pokemonId){
        this.team = team;
        this.pokemon = pokemon;
        this.pokemonId = pokemonId;
    }

    public void learnMove(Move move){
        movesLearnt.add(move);
    }
    public void removeMove(Move move){
        movesLearnt.remove(move);
    }

}

