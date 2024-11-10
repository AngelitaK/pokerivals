package com.smu.csd.pokerivals.betting.service;


import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.repository.TransactionPagingRepository;
import com.smu.csd.pokerivals.betting.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionPagingRepository transactionPagingRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionPagingRepository transactionPagingRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionPagingRepository = transactionPagingRepository;
    }

    public TransactionPageDTO getAllTransactions(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                transactionPagingRepository.findByChangeInCentsNotAndPlayer_Username(0, playerUsername, PageRequest.of(page,limit)),
                transactionRepository.countByChangeInCentsNotAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }
}
