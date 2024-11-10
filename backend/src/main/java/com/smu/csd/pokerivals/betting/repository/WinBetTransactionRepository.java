package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.Transaction;
import com.smu.csd.pokerivals.betting.entity.WinBetTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinBetTransactionRepository extends JpaRepository<WinBetTransaction, Transaction.TransactionID> {
    long countByChangeInCentsGreaterThanAndPlayer_Username(long amount, String username);
}

