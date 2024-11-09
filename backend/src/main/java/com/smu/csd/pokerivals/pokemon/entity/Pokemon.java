package com.smu.csd.pokerivals.pokemon.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pokemon {
    public Pokemon(){}

    @Id
    @EqualsAndHashCode.Include
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String name;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String type1;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String type2;


    @ManyToMany
    private Set <Ability> abilities = new HashSet<>();

    @Embedded
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Stats stats;

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Stats{
        private int HP;
        private int attack;
        private int defense;
        private int SpA;
        private int SpD;
        private int speed;

        @Override
        public String toString() {
            return "Stats{" +
                    "HP=" + HP +
                    ", attack=" + attack +
                    ", defense=" + defense +
                    ", SpA=" + SpA +
                    ", SpD=" + SpD +
                    ", speed=" + speed +
                    '}';
        }
    }


    @ManyToMany
    private Set<Move> moves = new HashSet<>();


    public Pokemon (Integer id , String name , String type1 , String type2 , Stats stats){
        this.id = id;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.stats = stats;
    }
    public void addMove(Move move){
        moves.add(move);
        move.addPokemon(this);
    }

    public void removeMove(Move move){
        moves.remove(move);
        move.removePokemon(this);
    }

    public void addAbility(Ability ability){
        abilities.add(ability);
        ability.addPokemon(this);
    }
    public void removeAbility(Ability ability){
        abilities.remove(ability);
        ability.removePokemon(this);
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type1='" + type1 + '\'' +
                ", type2='" + type2 + '\'' +
                ", abilities=" + abilities +
                ", stats=" + stats +
                ", moves=" + moves +
                '}';
    }
}
