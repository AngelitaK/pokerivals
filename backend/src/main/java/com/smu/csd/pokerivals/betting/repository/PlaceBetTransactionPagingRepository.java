package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.PlaceBetTransaction;
import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PlaceBetTransactionPagingRepository extends PagingAndSortingRepository<PlaceBetTransaction, Transaction.TransactionID> {
    List<Transaction> findByChangeInCentsAndPlayer_Username(long amount, String username, Pageable pageable);
    List<Transaction> findByChangeInCentsLessThanAndPlayer_Username(long amount, String username, Pageable pageable);
}
