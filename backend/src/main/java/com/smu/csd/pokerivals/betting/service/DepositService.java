package com.smu.csd.pokerivals.betting.service;

import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.entity.DepositTransaction;
import com.smu.csd.pokerivals.betting.repository.*;
import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DepositService {
    @Autowired
    public DepositService(
        PlayerRepository playerRepository,
        DepositTransactionRepository depositTransactionRepository,
        @Value("${stripe.secret-key}") String apiKey,
        DateFactory dateFactory,
        DepositTransactionPagingRepository depositTransactionPagingRepository,
        TransactionRepository transactionRepository
    ) {
        this.playerRepository = playerRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.dateFactory = dateFactory;
        this.depositTransactionPagingRepository = depositTransactionPagingRepository;
        this.transactionRepository = transactionRepository;
        Stripe.apiKey = apiKey;
    }

    @Value("${stripe.price-id.deposit-price-id}")
    private String depositPriceId;



    @Value("${frontend.origin}")
    private String origin;

    private final PlayerRepository playerRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final DateFactory dateFactory;
    private final DepositTransactionPagingRepository depositTransactionPagingRepository;
    private final TransactionRepository transactionRepository;

    public record HostedPaymentDTO( String link ){};
    @PreAuthorize("hasAuthority('PLAYER')")
    public HostedPaymentDTO createDepositCheckoutSessionHosted(String playerUsername) throws StripeException {
        var player = playerRepository.findById(playerUsername).orElseThrow();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setUiMode(SessionCreateParams.UiMode.HOSTED)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(depositPriceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setCustomerEmail(player.getEmail())
                        .setSuccessUrl(origin + "/success")
                        .setCancelUrl(origin + "/cancel")
                        .build();
        Session session = Session.create(params);

        var deposit = new DepositTransaction(session,player, dateFactory.getToday());
        deposit = depositTransactionRepository.save(deposit);

        return new HostedPaymentDTO(session.getUrl());
    }

    public record EmbeddedPaymentDTO( String clientSecret){};
    @PreAuthorize("hasAuthority('PLAYER')")
    public EmbeddedPaymentDTO createDepositCheckoutSessionEmbedded(String playerUsername) throws StripeException {
        var player = playerRepository.findById(playerUsername).orElseThrow();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setReturnUrl(origin+"/payment-done")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(depositPriceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setCustomerEmail(player.getEmail())
                        .build();
        Session session = Session.create(params);

        var deposit = new DepositTransaction(session,player, dateFactory.getToday());
        deposit = depositTransactionRepository.save(deposit);

        return new EmbeddedPaymentDTO(session.getClientSecret());
    }

    public static class SummarizedStripeCheckoutSessionDepositDTO{
        private final String checkoutSessionId;
        @Getter
        private final String customerEmail;
        private final String status;
        private final long amountTotal;

        public SummarizedStripeCheckoutSessionDepositDTO(Session session) {
            checkoutSessionId = session.getId();
            customerEmail = session.getCustomerEmail();
            status = session.getStatus();
            amountTotal = session.getAmountTotal();
        }
    }

    @Transactional
    public void updateDeposit(SummarizedStripeCheckoutSessionDepositDTO dto){
        var deposit = depositTransactionRepository.findOneByStripeCheckoutSessionId(dto.checkoutSessionId).orElseThrow();
        deposit.confirm(dto.customerEmail, dto.checkoutSessionId, dto.status, dto.amountTotal);
    }

    @PreAuthorize("hasAuthority('PLAYER')")
    public TransactionPageDTO getAllCompletedTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                depositTransactionPagingRepository.findByChangeInCentsGreaterThanAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                depositTransactionRepository.countByChangeInCentsGreaterThanAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }
    @PreAuthorize("hasAuthority('PLAYER')")
    public TransactionPageDTO getAllIncompleteTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                depositTransactionPagingRepository.findByChangeInCentsAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                depositTransactionRepository.countByChangeInCentsAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }

}
