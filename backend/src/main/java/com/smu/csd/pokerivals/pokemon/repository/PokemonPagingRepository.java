package com.smu.csd.pokerivals.pokemon.repository;

import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PokemonPagingRepository extends PagingAndSortingRepository<Pokemon,Integer> {

}
