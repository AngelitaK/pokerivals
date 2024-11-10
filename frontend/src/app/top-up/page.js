"use client";

import { useState, useCallback, useEffect } from "react";
import {
  Box,
  Flex,
  Stack,
  Button,
  Text,
  useToast,
  Tooltip,
  IconButton,
  Tabs, 
  TabList, 
  TabPanels, 
  Tab, 
  TabPanel,
  Select
} from "@chakra-ui/react";
import { InfoIcon } from "@chakra-ui/icons";
import axios from "../../../config/axiosInstance";
import TermsAndConditionsModal from "../../components/termsconditionModal";
import { useRouter } from "next/navigation";
import TransactionItem from "@/components/transactionItem";

const TopUpPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [transactions, setTransactions] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [balance, setBalance] = useState(0);
  const dollars = balance / 100;

  const [bettingTransactions, setBettingTransactions] = useState([]);
  const [bettingPage, setBettingPage] = useState(0);
  const [bettingHasMore, setBettingHasMore] = useState(true);

  const [selectedBetType, setSelectedBetType] = useState("active");
  const [activeBets, setActiveBets] = useState([]);
  const [activeBetsPage, setActiveBetsPage] = useState(0);
  const [activeBetsHasMore, setActiveBetsHasMore] = useState(true);

  const [ongoingBets, setOngoingBets] = useState([]);
  const [ongoingBetsPage, setOngoingBetsPage] = useState(0);
  const [ongoingBetsHasMore, setOngoingBetsHasMore] = useState(true);

  const [userTransactions, setUserTransactions] = useState([]);
  const [userTransactionsPage, setUserTransactionsPage] = useState(0);
  const [userTransactionsHasMore, setUserTransactionsHasMore] = useState(true);

  const toast = useToast();
  const router = useRouter();
  const limit = 2;

  // Fetch deposit transactions with pagination
  const fetchTransactions = useCallback(() => {
    axios
      .get("/transaction/deposit", {
        params: { page, limit, completed: true }
      })
      .then((response) => {
        const transactions = response.data.transactions;
        setTransactions(transactions);
        setHasMore(transactions.length === limit);
        setBalance(response.data.balance);
      })
      .catch((error) => {
        console.error("Error fetching transactions:", error);
        toast({
          title: "Error",
          description: "Failed to load transactions. Please try again.",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      });
  }, [page, limit, toast]);

  // Fetch betting transactions (using win endpoint) with pagination
  const fetchBettingTransactions = useCallback(() => {
    axios
      .get("/transaction/betting/win", {
        params: { page: bettingPage, limit }
      })
      .then((response) => {
        const transactions = response.data.transactions;
        setBettingTransactions(transactions);
        setBettingHasMore(transactions.length === limit);
      })
      .catch((error) => {
        console.error("Error fetching betting transactions:", error);
        toast({
          title: "Error",
          description: "Failed to load betting transactions. Please try again.",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      });
  }, [bettingPage, limit, toast]);

  // Fetch Active Bets with pagination (active=true)
  const fetchActiveBets = useCallback(() => {
    axios
      .get("/transaction/betting/placed", {
        params: { page: activeBetsPage, limit, active: true }
      })
      .then((response) => {
        const transactions = response.data.transactions;
        setActiveBets(transactions);
        setActiveBetsHasMore(transactions.length === limit);
      })
      .catch((error) => {
        console.error("Error fetching active bets:", error);
        toast({
          title: "Error",
          description: "Failed to load active bets. Please try again.",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      });
  }, [activeBetsPage, limit, toast]);

  // Fetch Ongoing Bets with pagination (active=false)
  const fetchOngoingBets = useCallback(() => {
    axios
      .get("/transaction/betting/placed", {
        params: { page: ongoingBetsPage, limit, active: false }
      })
      .then((response) => {
        const transactions = response.data.transactions;
        setOngoingBets(transactions);
        setOngoingBetsHasMore(transactions.length === limit);
      })
      .catch((error) => {
        console.error("Error fetching ongoing bets:", error);
        toast({
          title: "Error",
          description: "Failed to load ongoing bets. Please try again.",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      });
  }, [ongoingBetsPage, limit, toast]);

  // Fetch all user's transactions excluding those with no effect
  const fetchUserTransactions = useCallback(() => {
    axios
      .get("/transaction", {
        params: { page: userTransactionsPage, limit }
      })
      .then((response) => {
        const transactions = response.data.transactions;
        setUserTransactions(transactions);
        setUserTransactionsHasMore(transactions.length === limit);
      })
      .catch((error) => {
        console.error("Error fetching user transactions:", error);
        toast({
          title: "Error",
          description: "Failed to load user transactions. Please try again.",
          status: "error",
          duration: 2000,
          isClosable: true,
        });
      });
  }, [userTransactionsPage, limit, toast]);

  // Start hosted checkout session
  const startHostedCheckout = useCallback(() => {
    axios
      .post("/transaction/deposit/start/hosted")
      .then((response) => {
        const { link } = response.data;
        if (link) {
          window.location.href = link;
        } else {
          throw new Error("No link returned from Stripe");
        }
      })
      .catch((error) => {
        console.error("Error starting hosted checkout:", error);
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

  // Fetch transactions when component mounts or page changes
  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions, page]);

  useEffect(() => {
    fetchBettingTransactions();
  }, [fetchBettingTransactions, bettingPage]);

  useEffect(() => {
    if (selectedBetType === "active") {
      fetchActiveBets();
    } else {
      fetchOngoingBets();
    }
  }, [fetchActiveBets, fetchOngoingBets, selectedBetType, activeBetsPage, ongoingBetsPage]);

  useEffect(() => {
    fetchUserTransactions();
  }, [fetchUserTransactions, userTransactionsPage]);

  // Pagination handlers
  const handleNextPage = () => setPage((prevPage) => prevPage + 1);
  const handlePrevPage = () => setPage((prevPage) => (prevPage > 0 ? prevPage - 1 : 0));

  const handleNextBettingPage = () => setBettingPage((prevPage) => prevPage + 1);
  const handlePrevBettingPage = () => setBettingPage((prevPage) => (prevPage > 0 ? prevPage - 1 : 0));

  const handleNextUserTransactionsPage = () => setUserTransactionsPage((prevPage) => prevPage + 1);
  const handlePrevUserTransactionsPage = () => setUserTransactionsPage((prevPage) => (prevPage > 0 ? prevPage - 1 : 0));

  const handleBetTypeChange = (event) => {
    setSelectedBetType(event.target.value);
  };

  return (
    <Stack minH={"100vh"} bgImage="/TopupBG.png" bgSize="cover" bgPosition="center">
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
          {/* Balance and Tooltip section */}
          <Flex align="center" mt={5} direction="row" justify="center" gap={4} width="100%">
            <Flex align="center">
              <Text fontSize="3xl" fontWeight="bold">
                My Balance
              </Text>
            </Flex>

            <Tooltip label="SGD 1 = 🪙 100" fontSize="md">
              <Flex align="center" gap={2}>
                <Text fontSize="3xl" color="orange.400" fontWeight="bold">
                  🪙 {balance}
                </Text>
                <Text fontSize="md" color="gray.500">
                  (SGD {dollars})
                </Text>
              </Flex>
            </Tooltip>

            <Button bg="teal.500" color="white" px={4} py={2} borderRadius="md" fontWeight="bold" onClick={startHostedCheckout}>
              Top Up PokeCredits
            </Button>
            <IconButton icon={<InfoIcon />} aria-label="Info" size="sm" onClick={openModal} variant="ghost" color="gray.500" />
          </Flex>

          {/* My Transactions Section */}
          <Text fontSize="3xl" fontWeight="bold" mt={5}>
            My Transactions
          </Text>
          <Tabs variant="enclosed" mt={2} width="100%">
            <TabList>
              <Tab>Transactions</Tab>
              <Tab>Betting Transactions</Tab>
              <Tab>Active Bets</Tab>
              <Tab>Ongoing Bets</Tab>
              <Tab>User Transactions</Tab>
            </TabList>
            <TabPanels>
              {/* Transactions Tab */}
              <TabPanel p={0} mt={5} width="100%">
                {transactions.length === 0 ? (
                  <Text mt={5}>No transactions available.</Text>
                ) : (
                  transactions.map((transaction, index) => (
                    <TransactionItem key={index} transaction={transaction} />
                  ))
                )}
                {transactions.length > 0 && (
                  <Flex justify="space-between" mt={4}>
                    <Button onClick={handlePrevPage} isDisabled={page === 0}>
                      Previous
                    </Button>
                    <Button onClick={handleNextPage} isDisabled={!hasMore}>
                      Next
                    </Button>
                  </Flex>
                )}
              </TabPanel>

              {/* Betting Transactions Tab */}
              <TabPanel p={0} width="100%">
                {bettingTransactions.length === 0 ? (
                  <Text mt={5}>No betting transactions available.</Text>
                ) : (
                  bettingTransactions.map((transaction, index) => (
                    <TransactionItem key={index} transaction={transaction} />
                  ))
                )}
                {bettingTransactions.length > 0 && (
                  <Flex justify="space-between" mt={4}>
                    <Button onClick={handlePrevBettingPage} isDisabled={bettingPage === 0}>
                      Previous
                    </Button>
                    <Button onClick={handleNextBettingPage} isDisabled={!bettingHasMore}>
                      Next
                    </Button>
                  </Flex>
                )}
              </TabPanel>

              {/* Active Bets Tab */}
              <TabPanel p={0} width="100%">
                {activeBets.length === 0 ? (
                  <Text mt={5}>No active bets available.</Text>
                ) : (
                  activeBets.map((transaction, index) => (
                    <TransactionItem key={index} transaction={transaction} />
                  ))
                )}
                {activeBets.length > 0 && (
                  <Flex justify="space-between" mt={4}>
                    <Button onClick={handlePrevActiveBetsPage} isDisabled={activeBetsPage === 0}>
                      Previous
                    </Button>
                    <Button onClick={handleNextActiveBetsPage} isDisabled={!activeBetsHasMore}>
                      Next
                    </Button>
                  </Flex>
                )}
              </TabPanel>

              {/* Ongoing Bets Tab */}
              <TabPanel p={0} width="100%">
                {ongoingBets.length === 0 ? (
                  <Text mt={5}>No ongoing bets available.</Text>
                ) : (
                  ongoingBets.map((transaction, index) => (
                    <TransactionItem key={index} transaction={transaction} />
                  ))
                )}
                {ongoingBets.length > 0 && (
                  <Flex justify="space-between" mt={4}>
                    <Button onClick={handlePrevOngoingBetsPage} isDisabled={ongoingBetsPage === 0}>
                      Previous
                    </Button>
                    <Button onClick={handleNextOngoingBetsPage} isDisabled={!ongoingBetsHasMore}>
                      Next
                    </Button>
                  </Flex>
                )}
              </TabPanel>
              {/* User Transactions Tab */}
              <TabPanel p={0} width="100%" mt={5}>
                {userTransactions.length === 0 ? (
                  <Text mt={5}>No user transactions available.</Text>
                ) : (
                  userTransactions.map((transaction, index) => (
                    <TransactionItem key={index} transaction={transaction} />
                  ))
                )}
                {userTransactions.length > 0 && (
                  <Flex justify="space-between" mt={4}>
                    <Button onClick={handlePrevUserTransactionsPage} isDisabled={userTransactionsPage === 0}>
                      Previous
                    </Button>
                    <Button onClick={handleNextUserTransactionsPage} isDisabled={!userTransactionsHasMore}>
                      Next
                    </Button>
                  </Flex>
                )}
              </TabPanel>
            </TabPanels>
          </Tabs>
        </Flex>
      </Flex>
      <TermsAndConditionsModal isOpen={isModalOpen} onClose={closeModal} />
    </Stack>
  );
};

export default TopUpPage;
