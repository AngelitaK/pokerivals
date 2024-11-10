package com.smu.csd.pokerivals.payment;

import com.google.gson.JsonSyntaxException;
import com.smu.csd.pokerivals.NotificationService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private NotificationService notificationService;

    public PaymentTestController(@Value("${stripe.secret-key}") String apiKey) {
        log.info(apiKey);
        Stripe.apiKey = apiKey;
    }

    @Value("${stripe.price-id.deposit}")
    private String depositPriceId;

    @Value("${stripe.endpoint-secret}")
    private String endpointSecret;

    @PostMapping("/create-checkout-session")
    public Map<String,String> createCheckoutSession() throws StripeException {

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setReturnUrl("https://localhost:3000/return?session_id={CHECKOUT_SESSION_ID}")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(depositPriceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setCustomerEmail("abc@gmail.com")
                        .build();

        Session session = Session.create(params);

        Map<String, String> map = new HashMap<>();
//        map.put("clientSecret", session.getRawJsonObject().getAsJsonPrimitive("client_secret").getAsString());
        map.put("link", session.getUrl());
        return map;
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

    @PostMapping("/webhook")
    public void listenWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {
        Event event = null;

        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            System.out.println("⚠️  Webhook error while parsing basic request.");
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
                System.out.println("⚠️  Webhook error while validating signature.");
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
            log.info("Potential API Version Mismatch");
            throw new IllegalArgumentException();
        }
        // Handle the event
        if (event.getType().equals("checkout.session.completed")) {
            Session session = (Session) stripeObject;
            Map<String, String> map = new HashMap<>();
            map.put("status", session.getStatus());
            map.put("customer_email", session.getCustomerEmail());
            // amount is in cents!
            map.put("amount", session.getAmountTotal().toString());
            // Then define and call a method to handle the successful payment intent.
            notificationService.isThisActlAsync();
            log.info(map.toString());
            // handlePaymentIntentSucceeded(paymentIntent);
            // must be async!
        } else {
            System.out.println("Unhandled event type: " + event.getType());
        }
    }

}
