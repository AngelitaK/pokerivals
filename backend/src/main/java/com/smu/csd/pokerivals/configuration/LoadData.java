package com.smu.csd.pokerivals.configuration;

import com.opencsv.CSVReader;
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
  
  @Bean
  CommandLineRunner initDatabase(UserRepository userRepository, ClanRepository clanRepository) {
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