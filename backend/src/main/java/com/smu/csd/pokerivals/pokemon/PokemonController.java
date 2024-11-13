package com.smu.csd.pokerivals.pokemon;

import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pokemon")
@CrossOrigin
public class PokemonController {
    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("")
    @Operation(summary="Get All Pokemons")
    public PokemonService.PokemonPageDTO getPokemonsPaged(
            @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize)  {
        return pokemonService.getPokemonsByPage(page, pageSize);
    }

    @GetMapping("/{id}")
    @Operation(summary="Get One Pokemon", description="Registers an admin for an account")
    public Pokemon getOnePokemon(
            @PathVariable Integer id)  {
        return pokemonService.getPokemonById(id);
    }

    @GetMapping("/nature")
    @Operation(summary="Get Pokemon Nature", description="Registers an admin for an account")
    public List<String> pokemonNatures()  {
        return pokemonService.getPokemonNatures();
    }

    @GetMapping("/search")
    @Operation(summary="Search Pokemons")
    public PokemonService.PokemonPageDTO searchPokemon(
            @Parameter(description = "string to search pokemon by (case-insensitive)") @RequestParam("query") String query,
            @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize)  {
        return pokemonService.searchPokemon(query,page, pageSize);
    }

}
