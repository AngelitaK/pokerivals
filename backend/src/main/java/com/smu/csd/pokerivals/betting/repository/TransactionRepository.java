package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Transaction.TransactionID> {
    @Query("select coalesce( sum(t.changeInCents),0) from Transaction t where t.player.username = :username")
    long getPlayerBalance(String username);

    long countByChangeInCentsNotAndPlayer_Username(long amount, String username);
}
