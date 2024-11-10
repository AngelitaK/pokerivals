package com.smu.csd.pokerivals.betting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BettingSetting {
    @Id
    @Enumerated(EnumType.ORDINAL)
    private BettingSettingKey name;
    private int amount;
}
