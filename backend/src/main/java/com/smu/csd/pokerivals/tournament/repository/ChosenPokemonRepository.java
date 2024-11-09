package com.smu.csd.pokerivals.tournament.repository;

import com.smu.csd.pokerivals.tournament.entity.ChosenPokemon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChosenPokemonRepository extends JpaRepository<ChosenPokemon, ChosenPokemon.PokemonId> {
}
