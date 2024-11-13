package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.BettingSetting;
import com.smu.csd.pokerivals.betting.entity.BettingSettingKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BettingSettingRepository extends JpaRepository<BettingSetting, BettingSettingKey> {
}
