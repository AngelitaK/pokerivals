package com.smu.csd.pokerivals.pokemon.repository;

import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PokemonPagingRepository extends PagingAndSortingRepository<Pokemon,Integer> {
    public List<Pokemon> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
