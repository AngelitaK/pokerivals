package com.smu.csd.pokerivals.betting.service;

import com.smu.csd.pokerivals.match.entity.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class PaymentAsyncService {

    private final DepositService depositService;

    public PaymentAsyncService(DepositService depositService) {
        this.depositService = depositService;
    }

    @Async
    public void asynchronouslyUpdateDeposit(DepositService.SummarizedStripeCheckoutSessionDepositDTO dto){
        depositService.updateDeposit(dto);
        log.info("Processed deposit for {}", dto.getCustomerEmail());
    }

    @Async
    public void asyncProcessForfeit(Collection<Match.MatchId> matchIds){

    }
}
