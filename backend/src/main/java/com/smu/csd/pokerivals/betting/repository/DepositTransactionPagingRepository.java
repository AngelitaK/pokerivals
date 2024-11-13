package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.DepositTransaction;
import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DepositTransactionPagingRepository extends PagingAndSortingRepository<DepositTransaction, Transaction.TransactionID> {
    List<Transaction> findByChangeInCentsAndPlayer_Username(long amount, String username, Pageable pageable);
    List<Transaction> findByChangeInCentsGreaterThanAndPlayer_Username(long amount, String username, Pageable pageable);
}
