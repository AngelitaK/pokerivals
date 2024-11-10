package com.smu.csd.pokerivals.payment;

import com.smu.csd.pokerivals.record.Message;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment-test")
@CrossOrigin
public class PaymentTestController {

    public PaymentTestController(@Value("${stripe.secret-key}") String apiKey) {
        log.info(apiKey);
        Stripe.apiKey = apiKey;
    }

    @Value("${stripe.price-id.deposit}")
    private String depositPriceId;

    @PostMapping("/create-checkout-session")
    public Message createCheckoutSession() throws StripeException {

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setCancelUrl("https://example.com")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(depositPriceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("https://example.com/return?session_id={CHECKOUT_SESSION_ID}")
                        // to take from user repository
                        .setCustomerEmail("abc@gmail.com")
                        .build();

        Session session = Session.create(params);

        return new Message(session.getUrl());
    }

    @GetMapping("")
    public Map<String,String> checkCheckoutSession(@Param("checkout-id") String checkoutId ) throws StripeException {
        Session session = Session.retrieve(checkoutId);

        Map<String, String> map = new HashMap<>();
        map.put("status", session.getRawJsonObject().getAsJsonPrimitive("status").getAsString());
        map.put("customer_email", session.getRawJsonObject().getAsJsonObject("customer_details").getAsJsonPrimitive("email").getAsString());
        // amount is in cents!
        map.put("amount", session.getAmountTotal().toString());

        return map;
    }

}
