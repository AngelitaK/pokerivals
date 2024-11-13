package com.smu.csd.pokerivals.betting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
public class BettingSetting {
    @Id
    @Enumerated(EnumType.ORDINAL)
    private BettingSettingKey name;
    private int amount;

    public static double calculateProfitMargin(Collection<BettingSetting> settings, double winRate){
        int minimum_pi=0, maximum_pi=0;
        // when winRate is 0, profit margin is minimum

        int settingsConfigured = 0;
        for (BettingSetting s : settings){
            switch (s.getName()){
                case MINIMUM_PROFIT_MARGIN_PERCENTAGE -> {
                    minimum_pi = s.getAmount();
                    settingsConfigured++;
                }
                case MAXIMUM_PROFIT_MARGIN_PERCENTAGE -> {
                    maximum_pi = s.getAmount();
                    settingsConfigured++;
                }
            }
        }
        if(settingsConfigured < 2){
            throw new IllegalArgumentException("Settings error");
        }

        double gradient = maximum_pi - minimum_pi; // difference in x is 1
        double y_intersection = minimum_pi;

        return gradient * winRate + y_intersection;
    }
}
