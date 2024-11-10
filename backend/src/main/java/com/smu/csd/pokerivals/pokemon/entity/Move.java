package com.smu.csd.pokerivals.pokemon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class Move {
    
    @Id
    @Column(length = 100)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @EqualsAndHashCode.Include
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "moves")
    private Set <Pokemon> learntBy = new HashSet<>();

    public Move (String name){
        this.name = name;
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
                "name='" + name + '\'' +
                '}';
    }
}
