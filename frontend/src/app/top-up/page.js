"use client";

import { useState, useCallback } from "react";
import {
  Box,
  Flex,
  Stack,
  Button,
  Text,
  useToast,
  Tooltip,
  IconButton,
  Tabs, TabList, TabPanels, Tab, TabPanel
} from "@chakra-ui/react";
import { InfoIcon } from "@chakra-ui/icons";
import { loadStripe } from "@stripe/stripe-js";
import {
  EmbeddedCheckoutProvider,
  EmbeddedCheckout,
} from "@stripe/react-stripe-js";
import axios from "../../../config/axiosInstance";
import TermsAndConditionsModal from "../../components/termsconditionModal"; 
import { useRouter } from "next/navigation";

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY);

const TopUpPage = () => {
  const [clientSecret, setClientSecret] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const toast = useToast();
  const router = useRouter();

  const fetchClientSecret = useCallback(() => {
    axios.post("/deposit/start/embedded")
      .then((response) => {
        setClientSecret(response.data.clientSecret);
      })
      .catch((error) => {
        console.error("Error fetching client secret:", error);
        toast({
          title: "Error",
          description: "Failed to start top-up process. Please try again.",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      });
  }, [toast]);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  const options = { clientSecret };

  return (
    <Stack
      minH={"100vh"}
      bgImage="/TopupBG.png"
      bgSize="cover"
      bgPosition="center"
    >
      <Flex p="50px" justify="center">
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
          width="100%"
          maxWidth="1200px" 
        >
          {/* Balance and Tooltip section, always visible */}
          <Flex align="center" mt={5} direction="row" justify="center" gap={4} width="100%">
            <Flex align="center">
              <Text fontSize="3xl" fontWeight="bold">
                My Balance
              </Text>
            </Flex>

            <Tooltip label="SGD 1 = 🪙 100" fontSize="md">
              <Flex align="center" gap={2}>
                <Text fontSize="3xl" color="orange.400" fontWeight="bold">
                  🪙 1500
                </Text>
                <Text fontSize="md" color="gray.500">
                  (SGD15)
                </Text>
              </Flex>
            </Tooltip>

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
            <IconButton
              icon={<InfoIcon />}
              aria-label="Info"
              size="sm"
              onClick={openModal}
              variant="ghost"
              color="gray.500"
            />
          </Flex>

          {/* Conditional rendering for "My Transactions" and Tabs or the Embedded Checkout */}
          {clientSecret ? (
            <Box
              width="100%"
              maxWidth="900px"
              p={4}
              bg="white"
              borderRadius="lg"
              boxShadow="md"
              mt={5} // Add some margin on top for spacing
            >
              <EmbeddedCheckoutProvider stripe={stripePromise} options={{ clientSecret }}>
                <EmbeddedCheckout />
              </EmbeddedCheckoutProvider>
            </Box>
          ) : (
            <>
              <Text fontSize="3xl" fontWeight="bold" mt={5}>
                My Transactions
              </Text>
              <Tabs variant='enclosed' mt={2}>
                <TabList>
                  <Tab>One</Tab>
                  <Tab>Two</Tab>
                </TabList>
                <TabPanels>
                  <TabPanel>
                    <p>one!</p>
                  </TabPanel>
                  <TabPanel>
                    <p>two!</p>
                  </TabPanel>
                </TabPanels>
              </Tabs>
            </>
          )}
        </Flex>
      </Flex>
      <TermsAndConditionsModal isOpen={isModalOpen} onClose={closeModal} />
    </Stack>
  );
};

export default TopUpPage;
