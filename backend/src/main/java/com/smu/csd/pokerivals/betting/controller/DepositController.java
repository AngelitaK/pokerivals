package com.smu.csd.pokerivals.betting.controller;

import com.google.gson.JsonSyntaxException;
import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.service.DepositService;
import com.smu.csd.pokerivals.betting.service.PaymentAsyncService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/transaction/deposit")
@CrossOrigin
public class DepositController {

    private final PaymentAsyncService paymentAsyncService;
    private final DepositService depositService;

    @Autowired
    public DepositController(@Value("${stripe.secret-key}") String apiKey, PaymentAsyncService paymentAsyncService, DepositService depositService) {
        this.paymentAsyncService = paymentAsyncService;
        this.depositService = depositService;
        Stripe.apiKey = apiKey;
    }

    @GetMapping("")
    @Operation(summary = "Get all of the user's deposit either those who succeed and those who don't")
    public TransactionPageDTO getAllDepositTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "page of transaction to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of transaction per page") @RequestParam("limit") Integer pageSize,
            @Parameter(description = "whether to get deposits that succeed or not") @RequestParam("completed") boolean completed
    ){
        if (completed){
            return depositService.getAllCompletedTransaction(userDetails.getUsername(),page,pageSize);
        }
        return depositService.getAllIncompleteTransaction(userDetails.getUsername(),page,pageSize);
    }

    @PostMapping("/start/embedded")
    @Operation(summary = "Start a deposit Stripe checkout session embedded in frontend")
    public DepositService.EmbeddedPaymentDTO depositViaEmbeddedForm(
            @AuthenticationPrincipal UserDetails userDetails
            ) throws StripeException
    {
        return depositService.createDepositCheckoutSessionEmbedded(userDetails.getUsername());
    }

    @PostMapping("/start/hosted")
    @Operation(summary = "Start a deposit Stripe checkout session in page hosted by Stripe")
    public DepositService.HostedPaymentDTO depositViaHostedForm(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws StripeException
    {
        return depositService.createDepositCheckoutSessionHosted(userDetails.getUsername());
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook to be called by Stripe API")
    public void listenWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader,
            @Value("${stripe.endpoint-secret}") String endpointSecret
    ) {
        // taken from Stripe documentation
        Event event = null;
        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            log.error("⚠️  Webhook error while parsing basic request.");
            throw new IllegalArgumentException();
        }
        if(endpointSecret != null && sigHeader != null) {
            // Only verify the event if you have an endpoint secret defined.
            // Otherwise, use the basic event deserialized with GSON.
            try {
                event = Webhook.constructEvent(
                        payload, sigHeader, endpointSecret
                );
            } catch (SignatureVerificationException e) {
                // Invalid signature
                log.error("⚠️  Webhook error while validating signature.");
                throw new IllegalArgumentException();
            }
        }
        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
            log.error("Potential API Version Mismatch");
            throw new IllegalArgumentException();
        }
        // Handle the event
        if (event.getType().equals("checkout.session.completed")) {
            Session session = (Session) stripeObject;
            paymentAsyncService.asynchronouslyUpdateDeposit(new DepositService.SummarizedStripeCheckoutSessionDepositDTO(session));
        } else {
            log.trace("Unhandled event type: {}", event.getType());
        }
    }

}
