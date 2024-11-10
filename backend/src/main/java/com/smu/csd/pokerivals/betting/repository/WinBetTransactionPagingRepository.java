package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.Transaction;
import com.smu.csd.pokerivals.betting.entity.WinBetTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WinBetTransactionPagingRepository extends PagingAndSortingRepository<WinBetTransaction, Transaction.TransactionID> {
    List<Transaction> findByChangeInCentsGreaterThanAndPlayer_Username(long amount, String username, Pageable pageable);
}

