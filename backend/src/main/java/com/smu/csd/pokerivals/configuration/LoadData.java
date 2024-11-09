package com.smu.csd.pokerivals.configuration;

import com.opencsv.CSVReader;
import com.smu.csd.pokerivals.pokemon.entity.Ability;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.AbilityRepository;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Clan;
import com.smu.csd.pokerivals.user.entity.ClanRepository;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Configuration
class LoadData {

  private final int noPokemonsForTest = 100;

  @Bean
  CommandLineRunner initDatabase(UserRepository userRepository, ClanRepository clanRepository, PokemonRepository pokemonRepository, AbilityRepository abilityRepository, MoveRepository moveRepository) {
    return args -> {
      // Load Clan
      File file = ResourceUtils.getFile("classpath:clan.csv");
      try (Reader reader = Files.newBufferedReader(file.toPath())) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            clanRepository.deleteById(line[0]);
            System.out.println("Saving " + clanRepository.save(new Clan(line[0].toLowerCase())));
          }
        }
      }

      // Load Admin
      file = ResourceUtils.getFile("classpath:admins.csv");
      try (Reader reader = Files.newBufferedReader(file.toPath())) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            Admin admin = new Admin(line[0], line[1]);
            if (userRepository.findById(admin.getId()).isEmpty() && userRepository.findOneByGoogleSub(admin.getGoogleSub()).isEmpty()) {
              userRepository.deleteById(admin.getUsername());
              userRepository.deleteByGoogleSub(admin.getGoogleSub());
              System.out.println("Saving Admin : " + userRepository.save(admin));
            }
          }
        }
      }

      // Load Players
      file = ResourceUtils.getFile("classpath:players.csv");
      try (Reader reader = Files.newBufferedReader(file.toPath())) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            Player player = new Player(line[0], line[1]);
            if (userRepository.findById(player.getId()).isEmpty() && userRepository.findOneByGoogleSub(player.getGoogleSub()).isEmpty()){
              userRepository.deleteById(player.getUsername());
              userRepository.deleteByGoogleSub(player.getGoogleSub());
              System.out.println("Saving Player : " + userRepository.save(player));
            }
          }
        }
      }

      // Load pokemon
      file = ResourceUtils.getFile("classpath:pokemon.csv");
      List<Map<String,String>> pokemonRows = new ArrayList<>();

      try (Reader reader = Files.newBufferedReader(file.toPath())) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            pokemonRows.add(convertArraysToMap(header,line));
          }
        }
      }

      file = ResourceUtils.getFile("classpath:learnset_data.csv");
      Map<String, Set<String>> pokemonMoves = new HashMap<>();

      try (Reader reader = Files.newBufferedReader(file.toPath())) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            pokemonMoves.computeIfAbsent(line[0], k->new HashSet<>()).add(line[1]);
          }
        }
      }

      int noPokemonsInserted = 0;
      boolean isTest = false;
      String[] activeProfiles = environment.getActiveProfiles();
      if (activeProfiles.length > 0) {
        isTest=true;
      }

      if (pokemonRepository.count() == pokemonRows.size()){
        return;
      }

      for(Map<String,String> row : pokemonRows){
        System.out.println(row);

        String[] type = new String[2];
        String typeString = row.get("types");
        if(typeString.contains(",")){
          type = typeString.split(",");
        } else{
          type = new String[]{typeString,null};
        }

        Pokemon.Stats stats = new Pokemon.Stats(
                Integer.parseInt(row.get("HP")),
                Integer.parseInt(row.get("attack")),
                Integer.parseInt(row.get("defense")),
                Integer.parseInt(row.get("SpA")),
                Integer.parseInt(row.get("SpD")),
                Integer.parseInt(row.get("speed"))
        );

        List<Ability> abilities = new ArrayList<>();
        if(!row.get("Ability0").isEmpty()){
          abilities.add(new Ability(row.get("Ability0")));
        }

        if(!row.get("HiddenAbility").isEmpty()){
          abilities.add(new Ability(row.get("HiddenAbility")));
        }

        for (Ability ability : abilities){
          abilityRepository.save(ability);
        }

        Pokemon pokemon = new Pokemon(
                Integer.parseInt(row.get("id")),
                row.get("name"),
                type[0],
                type[1],
                stats
        );
        for (Ability ability : abilities){
          pokemon.addAbility(ability);
        }

        Set<String>  moveNames = pokemonMoves.get(pokemon.getName());
        final List<Move> moves = new ArrayList<>();

        for(String moveName: moveNames){
          Move move = new Move(moveName);
          moveRepository.save(move);
          moves.add(move);
        }

        for (Move move : moves){
          pokemon.addMove(move);
        }

        pokemonRepository.deleteById(pokemon.getId());
        log.info("Preloading " + pokemonRepository.save(pokemon));

        if(isTest && ++noPokemonsInserted > noPokemonsForTest){
          break;
        }

      }




    };
  }

  @Autowired
  private Environment environment;

  private <K,V> Map<K,V> convertArraysToMap(K[] keys, V[] values){
    Map<K,V> result = new HashMap<>();

    for (int i = 0; i < keys.length; i++){
      result.put(keys[i], values[i]);
    }

    return result;

  }
}