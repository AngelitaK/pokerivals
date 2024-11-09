package com.smu.csd.pokerivals.pokemon;

import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.PokemonPagingRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final PokemonPagingRepository pokemonPagingRepository;

    @Autowired
    public PokemonService(PokemonRepository pokemonRepository, PokemonPagingRepository pokemonPagingRepository){
        this.pokemonRepository = pokemonRepository;
        this.pokemonPagingRepository = pokemonPagingRepository;
    }

    public Pokemon getPokemonById(int id){
        return pokemonRepository.findById(id).orElseThrow();
    }

    public record PokemonPageDTO(List<Pokemon> pokemons, long count) {}

    public PokemonPageDTO getPokemonsByPage(int page, int limit){
        return new PokemonPageDTO(
                pokemonPagingRepository.findAll(PageRequest.of(page,limit)).getContent(),
                pokemonRepository.count()
        );
    }
}

