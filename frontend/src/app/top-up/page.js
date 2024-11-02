"use client";

import { useState, useCallback, useEffect } from "react";
import {
  Box,
  Flex,
  Stack,
  Button,
  Heading,
  Text,
  Grid,
  useToast
} from "@chakra-ui/react";
import { loadStripe } from "@stripe/stripe-js";
import {
  EmbeddedCheckoutProvider,
  EmbeddedCheckout,
} from "@stripe/react-stripe-js";
import axios from "../../../config/axiosInstance";
import { useRouter } from "next/navigation";

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY);

const TopUpPage = () => {
  const [clientSecret, setClientSecret] = useState(null);
  const toast = useToast();
  const router = useRouter();

  const fetchClientSecret = useCallback(() => {
    axios.post("/payment-test/create-checkout-session")
      .then((response) => {
        setClientSecret(response.data.clientSecret);
      })
      .catch((error) => {
        console.error("Error fetching client secret:", error);
      });
  });

  // Check for session_id in the URL and fetch payment status
  // useEffect(() => {
  //   const { session_id } = router.query;

  //   if (session_id) {
  //     axios
  //       .get(`/payment-test?checkoutId=${session_id}`)
  //       .then((response) => {
  //         const { status } = response.data;
  //         if (status === "complete") {
  //           toast({
  //             title: "Top-up Successful!",
  //             description: "Your top-up has been successfully processed.",
  //             status: "success",
  //             duration: 5000,
  //             isClosable: true,
  //           });
  //         }
  //       })
  //       .catch((error) => {
  //         console.error("Error checking payment status:", error);
  //       });
  //   }
  // }, [router.query, toast]);

  const options = { clientSecret };

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
            {clientSecret ? (
              <Box
              width="110%"
              maxWidth="900px"
              p={4}
              bg="white"
              borderRadius="lg"
              boxShadow="md"
            >
                <EmbeddedCheckoutProvider stripe={stripePromise} options={{ clientSecret }}>
                  <EmbeddedCheckout />
                </EmbeddedCheckoutProvider>
              </Box>
            ) : (
              <>
                <Text fontSize="3xl" fontWeight="bold">
                  Current PokeCredits
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

                <Flex gap={4} mt={5}>
                  <Button
                    bg="teal.500"
                    color="white"
                    px={4}
                    py={2}
                    borderRadius="md"
                    fontWeight="bold"
                    onClick={fetchClientSecret}
                  >
                    Top Up PokeCredits
                  </Button>
                </Flex>
              </>
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
