package com.smu.csd.pokerivals.pokemon.repository;

import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {
    Optional<Pokemon> findByName(String name);
}
