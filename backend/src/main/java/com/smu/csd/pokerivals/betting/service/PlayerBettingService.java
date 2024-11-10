package com.smu.csd.pokerivals.betting.service;

import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.repository.*;
import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAuthority('PLAYER')")
public class PlayerBettingService {
    private final PlayerRepository playerRepository;
    private final DateFactory dateFactory;

    @Autowired
    public PlayerBettingService(PlayerRepository playerRepository, DateFactory dateFactory, PlaceBetTransactionRepository placeBetTransactionRepository, PlaceBetTransactionPagingRepository placeBetTransactionPagingRepository, WinBetTransactionRepository winBetTransactionRepository, WinBetTransactionPagingRepository winBetTransactionPagingRepository, TransactionRepository transactionRepository) {
        this.playerRepository = playerRepository;
        this.dateFactory = dateFactory;
        this.placeBetTransactionRepository = placeBetTransactionRepository;
        this.placeBetTransactionPagingRepository = placeBetTransactionPagingRepository;
        this.winBetTransactionRepository = winBetTransactionRepository;
        this.winBetTransactionPagingRepository = winBetTransactionPagingRepository;
        this.transactionRepository = transactionRepository;
    }

    private final PlaceBetTransactionRepository placeBetTransactionRepository;
    private final PlaceBetTransactionPagingRepository placeBetTransactionPagingRepository;
    private final WinBetTransactionRepository winBetTransactionRepository;
    private final WinBetTransactionPagingRepository winBetTransactionPagingRepository;
    private final TransactionRepository transactionRepository;

    public TransactionPageDTO getAllActiveBetsPlacedTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                placeBetTransactionPagingRepository.findByChangeInCentsLessThanAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                placeBetTransactionRepository.countByChangeInCentsLessThanAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }
    public TransactionPageDTO getAllInactiveBetsPlacedTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                placeBetTransactionPagingRepository.findByChangeInCentsAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                placeBetTransactionRepository.countByChangeInCentsAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }

    public TransactionPageDTO getAllWinBetTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                winBetTransactionPagingRepository.findByChangeInCentsGreaterThanAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                winBetTransactionRepository.countByChangeInCentsGreaterThanAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }

    public void placeBet(){

    }
    public void modifyBet(){

    }

    // and the notification class
    public void winBet(){
        // create win bet for everyone
        // notify
    }
    public void loseBet(){
        // notify
    }



}
