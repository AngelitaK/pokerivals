package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.PlaceBetTransaction;
import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceBetTransactionRepository extends JpaRepository<PlaceBetTransaction, Transaction.TransactionID> {
    long countByChangeInCentsAndPlayer_Username(long amount, String username);
    long countByChangeInCentsLessThanAndPlayer_Username(long amount, String username);
}
