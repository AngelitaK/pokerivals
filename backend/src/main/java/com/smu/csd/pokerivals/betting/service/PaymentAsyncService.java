package com.smu.csd.pokerivals.betting.service;

import com.smu.csd.pokerivals.match.entity.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentAsyncService {

    private final DepositService depositService;
    private final PlayerBettingService playerBettingService;

    @Autowired
    public PaymentAsyncService(DepositService depositService, PlayerBettingService playerBettingService) {
        this.depositService = depositService;
        this.playerBettingService = playerBettingService;
    }

    @Async
    public void asynchronouslyUpdateDeposit(DepositService.SummarizedStripeCheckoutSessionDepositDTO dto){
        depositService.updateDeposit(dto);
        log.info("Processed deposit for {}", dto.getCustomerEmail());
    }

    @Async
    public void asyncProcessForfeit(Match.MatchId matchId){
        playerBettingService.processForfeitOrCancel(matchId);
    }

    public void asyncWinBet(Match.MatchId matchId){
        playerBettingService.winBet(matchId);
    }
}
