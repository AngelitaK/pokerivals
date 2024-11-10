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
public class Ability {
    public Ability(String name){
        this.name = name;
    }
    @Id
    @Column(length = 100)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @EqualsAndHashCode.Include
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "abilities")
    private Set<Pokemon> possessedBy = new HashSet<>();
    
    public void addPokemon (Pokemon pokemon){
        possessedBy.add(pokemon);
    }

    public void removePokemon(Pokemon pokemon){
        possessedBy.remove(pokemon);
    }

    @Override
    public String toString() {
        return "Ability{" +
                "name='" + name + '\'' +
                '}';
    }
}
