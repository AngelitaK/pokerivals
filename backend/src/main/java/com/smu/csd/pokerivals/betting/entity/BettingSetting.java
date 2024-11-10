package com.smu.csd.pokerivals.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BettingSetting {
    @Id
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(length = 100, name="`key`")
    private BettingSettingKey key;
    private int value;
}
