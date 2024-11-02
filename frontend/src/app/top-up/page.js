"use client";

import { useState, useCallback } from "react";
import {
  Box,
  Flex,
  Stack,
  Button,
  Heading,
  Text,
  Grid,
  Input,
} from "@chakra-ui/react";
import {loadStripe} from '@stripe/stripe-js';
import {
  EmbeddedCheckoutProvider,
  EmbeddedCheckout
} from '@stripe/react-stripe-js';
import axios from '../../../config/axiosInstance'

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY);

const TopUpPage = () => {
  const [amount, setAmount] = useState(0);
  const [credits, setCredits] = useState(0);
  const [clientSecret, setClientSecret] = useState(null);

  const handleAmountChange = (e) => {
    const newAmount = parseInt(e.target.value) || 0;
    setAmount(newAmount);
    setCredits(newAmount * 100); 
  };

  // Fetch client secret from backend on payment initiation
  const fetchClientSecret = useCallback(() => {
    axios.post("/payment-test/create-checkout-session", { amount })
      .then((response) => {
        setClientSecret(response.data.clientSecret); // Set clientSecret for Embedded Checkout
      })
      .catch((error) => {
        console.error("Error fetching client secret:", error);
      });
  }, [amount]);

  const options = { fetchClientSecret };

  return (
    <Stack
      minH={"100vh"}
      bgImage="/TopupBG.png"
      bgSize="cover"
      bgPosition="center"
    >
      <Flex p="50px">
        <Grid templateColumns="repeat(2, 1fr)" gap={6} w="100%" h="100%">
          <Flex
            bg="rgba(255, 255, 255, 0.8)"
            p={10}
            borderRadius="2xl"
            boxShadow="md"
            mb={6}
            gap={3}
            direction="column"
            justify="center"  
            align="center"
            color="black"
          >
            <Text fontSize="3xl" fontWeight="bold">
                Current Credits
            </Text>
            <Flex align="center" gap={2}>
                <Text fontSize="3xl" color="orange.400" fontWeight="bold">
                🪙 1500
                </Text>
                <Text fontSize="md" color="gray.500">
                (SGD15)
                </Text>
            </Flex>

            <Text fontSize="lg" fontWeight="bold" mt={4}>
                Conversion Rate
            </Text>
            <Text fontSize="sm" color="gray.600">
                SGD 1 = 🪙 100
            </Text>

            <Flex justify="space-between" width="100%" mt={2} px={5}>
                <Box textAlign="center">
                    <Text fontSize="sm" fontWeight="semibold" color="gray.700">
                        Amount Payable
                    </Text>
                    <Input
                        type="number"
                        placeholder="SGD"
                        value={amount}
                        onChange={handleAmountChange}
                        border="1px solid"
                        borderColor="gray.300"
                        borderRadius="md"
                        padding="8px"
                        width="60px"
                        textAlign="center"
                    />
                </Box>

                <Text fontSize="2xl" fontWeight="bold">
                    =
                </Text>

                <Box textAlign="center">
                    <Text fontSize="sm" fontWeight="semibold" color="gray.700">
                        Credits Received
                    </Text>
                    <Text fontSize="lg" fontWeight="bold" color="orange.400">
                        🪙 {credits}
                    </Text>
                </Box>
            </Flex>

                <Flex gap={4} mt={5}>
                    <Box as="button" bg="teal.500" color="white" px={4} py={2} borderRadius="md" fontWeight="bold">
                    Make Payment
                    </Box>
                </Flex>
                {clientSecret && (
                    <Box mt={6} p={4} bg="white" borderRadius="lg" boxShadow="md" width="100%">
                    <EmbeddedCheckoutProvider stripe={stripePromise} options={{ clientSecret }}>
                        <EmbeddedCheckout />
                    </EmbeddedCheckoutProvider>
                    </Box>
                )}
          </Flex>

          <Flex
            bg="rgba(255, 255, 255, 0.8)"
            p={10}
            borderRadius="2xl"
            boxShadow="md"
            mb={6}
            gap={3}
            direction="column"
          >
            <Flex direction="row" gap={5}>
                <Heading textAlign="center" size="md">Terms & Conditions</Heading>
            </Flex>
            <Text fontWeight="bold">1. Acceptance of Terms</Text>
            <Text>
                By using the top-up service to purchase PokeCredits, you acknowledge and agree to these terms and conditions. Please read them carefully.
            </Text>

            <Text fontWeight="bold">2. No Refunds</Text>
            <Text>
                All purchases of PokeCredits are final and non-refundable. Once you have confirmed your transaction and PokeCredits have been credited to your account, you will not be able to reverse or refund the transaction.
            </Text>

            <Text fontWeight="bold">3. User Responsibility</Text>
            <Text>
                - You are responsible for ensuring that any funds used for purchasing PokeCredits are legally obtained and owned by you. <br/>
                - Only individuals aged 18 or older (or the age of legal consent in your jurisdiction) are permitted to participate in purchasing PokeCredits. <br/>
            </Text>

            <Text fontWeight="bold">4. Betting Risks</Text>
            <Text>
                All bets placed using PokeCredits are at your own risk. PokeCredits used for betting are not refundable, even if you lose the bet. The platform is not responsible for any losses incurred while using PokeCredits for betting or other activities on the platform.
            </Text>
          </Flex>
        </Grid>
      </Flex>
    </Stack>
  );
};

export default TopUpPage;
