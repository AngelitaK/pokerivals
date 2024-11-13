package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.DepositTransaction;
import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Transaction.TransactionID> {
    Optional<DepositTransaction> findOneByStripeCheckoutSessionId(String sessionId);

    long countByChangeInCentsAndPlayer_Username(long amount, String username);
    long countByChangeInCentsGreaterThanAndPlayer_Username(long amount, String username);
}
