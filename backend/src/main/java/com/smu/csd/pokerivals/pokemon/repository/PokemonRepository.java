package com.smu.csd.pokerivals.pokemon.repository;

import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {
    Optional<Pokemon> findByName(String name);

    @NotNull
    @EntityGraph(attributePaths = { "abilities", "moves" })
    Optional<Pokemon> findById(@NotNull Integer id);

    public long countByNameContainingIgnoreCase(String name);
}
