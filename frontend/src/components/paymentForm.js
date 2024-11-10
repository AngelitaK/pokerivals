'use client'

import * as React from 'react';
import {loadStripe} from '@stripe/stripe-js';
import {
  EmbeddedCheckoutProvider,
  EmbeddedCheckout
} from '@stripe/react-stripe-js';

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY);

const PaymentPage = () => {
    const fetchClientSecret = useCallback(() => {
        // Create a Checkout Session by calling the backend
        return fetch("/api/create-checkout-session", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          // Include any other necessary data for creating a checkout session
          body: JSON.stringify({ amount: 2000 }) // example amount in cents
        })
          .then((res) => res.json())
          .then((data) => data.clientSecret);
    }, [amount]);

  const options = { fetchClientSecret };

  return (
    <div id="checkout">
      <EmbeddedCheckoutProvider
        stripe={stripePromise}
        options={options}
      >
        <EmbeddedCheckout />
      </EmbeddedCheckoutProvider>
    </div>
  )
}

export default PaymentPage;