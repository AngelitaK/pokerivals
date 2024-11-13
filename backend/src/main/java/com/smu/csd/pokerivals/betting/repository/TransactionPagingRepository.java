package com.smu.csd.pokerivals.betting.repository;

import com.smu.csd.pokerivals.betting.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TransactionPagingRepository extends PagingAndSortingRepository<Transaction, Transaction.TransactionID> {
    List<Transaction> findByChangeInCentsNotAndPlayer_Username(long amount, String username, Pageable pageable);
}
