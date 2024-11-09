package com.smu.csd.pokerivals.pokemon.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Move {
    public Move(){}

    @Id
    @Column(length = 100)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String move_name;

    @Override
    public boolean equals(Object other){
        if (other instanceof Move o){
            return move_name.equals(o.getMove_name());
        }
        return false;
    }

    @ManyToMany(mappedBy = "moves")
    private Set <Pokemon> learntBy = new HashSet<>();

    public Move (String move_name){
        this.move_name = move_name;
    }

    public void addPokemon(Pokemon pokemon){
        learntBy.add(pokemon);
    }
    public void removePokemon(Pokemon pokemon){
        learntBy.remove(pokemon);
    }

    @Override
    public String toString() {
        return "Move{" +
                "move_name='" + move_name + '\'' +
                '}';
    }
}
