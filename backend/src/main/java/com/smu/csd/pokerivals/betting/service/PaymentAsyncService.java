package com.smu.csd.pokerivals.betting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
}
