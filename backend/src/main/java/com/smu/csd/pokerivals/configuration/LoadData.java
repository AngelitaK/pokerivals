package com.smu.csd.pokerivals.configuration;

import com.opencsv.CSVReader;
import com.smu.csd.pokerivals.betting.entity.BettingSetting;
import com.smu.csd.pokerivals.betting.entity.BettingSettingKey;
import com.smu.csd.pokerivals.betting.repository.BettingSettingRepository;
import com.smu.csd.pokerivals.pokemon.entity.Ability;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.AbilityRepository;
import com.smu.csd.pokerivals.pokemon.repository.MoveRepository;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Clan;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.ClanRepository;
import com.smu.csd.pokerivals.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Slf4j
@Configuration
public class LoadData {

  private final int noPokemonsForTest = 100;

  @Autowired
  public LoadData(Environment environment) {
    this.environment = environment;
  }

  @Bean
  public InitializingBean initDatabase(
          UserRepository userRepository,
          ClanRepository clanRepository,
          PokemonRepository pokemonRepository,
          AbilityRepository abilityRepository,
          MoveRepository moveRepository,
          BettingSettingRepository bettingSettingRepository
  ) {
    return () -> {
      // load config
      var minConfig = bettingSettingRepository.findById(BettingSettingKey.MINIMUM_PROFIT_MARGIN_PERCENTAGE);
      var maxConfig = bettingSettingRepository.findById(BettingSettingKey.MAXIMUM_PROFIT_MARGIN_PERCENTAGE);
      if (minConfig.isEmpty()){
        bettingSettingRepository.save(new BettingSetting(BettingSettingKey.MINIMUM_PROFIT_MARGIN_PERCENTAGE, 5));
      }
      if (maxConfig.isEmpty()){
        bettingSettingRepository.save(new BettingSetting(BettingSettingKey.MAXIMUM_PROFIT_MARGIN_PERCENTAGE,10));
      }



      // Load Clan
      Resource r = new ClassPathResource("clan.csv");
      try (InputStreamReader isr = new InputStreamReader(r.getInputStream()); Reader reader =  new BufferedReader(isr)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            if (clanRepository.findById(line[0].toLowerCase()).isEmpty()) {
              log.trace("Saving {}", clanRepository.save(new Clan(line[0].toLowerCase())));
            }
          }
        }
      }

      // Load Admin

      r = new ClassPathResource("admins.csv");
      try (InputStreamReader isr = new InputStreamReader(r.getInputStream()); Reader reader =  new BufferedReader(isr)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            Admin admin = new Admin(line[0], line[1]);
            if (userRepository.findById(admin.getId()).isEmpty() && userRepository.findOneByGoogleSub(admin.getGoogleSub()).isEmpty()) {
              userRepository.deleteById(admin.getUsername());
              userRepository.deleteByGoogleSub(admin.getGoogleSub());
              log.trace("Saving Admin : {}", userRepository.save(admin));
            }
          }
        }
      }

      // Load Players
      r = new ClassPathResource("players.csv");
      try (InputStreamReader isr = new InputStreamReader(r.getInputStream()); Reader reader =  new BufferedReader(isr)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            Player player = new Player(line[0], line[1]);
            if (userRepository.findById(player.getId()).isEmpty() && userRepository.findOneByGoogleSub(player.getGoogleSub()).isEmpty()){
              userRepository.deleteById(player.getUsername());
              userRepository.deleteByGoogleSub(player.getGoogleSub());
              log.trace("Saving Player : {}", userRepository.save(player));
            }
          }
        }
      }

      // Load pokemon

      List<Map<String,String>> pokemonRows = new ArrayList<>();
      r = new ClassPathResource("pokemon.csv");
      try (InputStreamReader isr = new InputStreamReader(r.getInputStream()); Reader reader =  new BufferedReader(isr)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
          String[] header = csvReader.readNext();
          String[] line;
          while ((line = csvReader.readNext()) != null) {
            pokemonRows.add(convertArraysToMap(header,line));
          }
        }
      }

      Map<String, Set<String>> pokemonMoves = new HashMap<>();

      r = new ClassPathResource("learnset_data.csv");
      try (InputStreamReader isr = new InputStreamReader(r.getInputStream()); Reader reader =  new BufferedReader(isr)) {
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
      for (String s: activeProfiles){
        if (s.equals("test")){
          isTest = true;
          break;
        }
      }

      if (pokemonRepository.count() == pokemonRows.size()){
        return;
      }

      for(Map<String,String> row : pokemonRows){

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

        log.info("Preloading " + pokemonRepository.save(pokemon));

        if(isTest && ++noPokemonsInserted > noPokemonsForTest){
          break;
        }
      }
    };
  }

  private final Environment environment;

  private <K,V> Map<K,V> convertArraysToMap(K[] keys, V[] values){
    Map<K,V> result = new HashMap<>();

    for (int i = 0; i < keys.length; i++){
      result.put(keys[i], values[i]);
    }

    return result;

  }
}