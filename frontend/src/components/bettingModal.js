"use client";
import { useEffect, useState } from "react";
import {
  Flex,
  Text,
  Button,
  Stack,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  Select,
  Input,
  useToast,
} from "@chakra-ui/react";
import axios from "../../config/axiosInstance";

const BetModal = ({ isOpen, onClose, details }) => {
  console.log(details);
  const toast = useToast();
  const teamA = details.teams[0].id;
  const teamB = details.teams[1].id;
  const odds1 = details.teams[0].winRate * 100;
  const odds2 = details.teams[1].winRate * 100;

  const [selectedPlayer, setSelectedPlayer] = useState("TEAM_A");
  const [betAmount, setBetAmount] = useState(0);
  const [expectedWin, setExpectedWin] = useState(0);

  // Update the prediction whenever the betAmount or selectedPlayer changes
  useEffect(() => {
    const fetchPrediction = async () => {
      if (betAmount <= 0) {
        setExpectedWin(0); // Reset expected win if bet amount is invalid
        return;
      }
  
      try {
        // Prepare request body
        const requestBody = {
          matchId: {
            tournamentId: details.tournament_id,
            depth: details.depth,
            index: details.index,
          },
          betAmountInCents: parseInt(betAmount) * 100, // Ensure betAmount is parsed to an integer
          side: selectedPlayer,
        };
  
        console.log("Request Body:", requestBody); // Log the request body
  
        // Send PATCH request with the request body
        const response = await axios.patch("/transaction/betting/bet", requestBody);
        console.log("Response Data:", response.data); // Log the response data
  
        if (response.status === 200) {
          const data = response.data;
          // Ensure expectedWin is updated correctly
          setExpectedWin(data.winAmountInCents); 
        } else {
          throw new Error("Failed to fetch prediction data");
        }
      } catch (error) {
        console.error("Error fetching prediction:", error);
        setExpectedWin(0); // Reset expected win in case of error
      }
    };
  
    fetchPrediction();
  });
  console.log("Bet Amount Changed:", betAmount);

  
  const handleConfirm = async () => {
    const betAmountInCents = parseInt(betAmount) * 100;

    if (betAmountInCents <= 0) {
      toast({
        title: "Error",
        description: "Bet amount must be greater than 0",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
      return; // Prevent placing bet if amount is invalid
    }

    try {
      const response = await axios.post("/transaction/betting/bet", {
        matchId: {
          tournamentId: details.tournament_id,
          depth: details.depth,
          index: details.index,
        },
        betAmountInCents: betAmountInCents,
        side: selectedPlayer,
      });

      if (response.status === 200) {
        toast({
          title: "Good Luck! 🎉",
          description: "Bet amount successful!",
          status: "success",
          duration: 5000,
          isClosable: true,
        });
      } else {
        toast({
          title: "Error",
          description: "Failed to place bet.",
          status: "error",
          duration: 5000,
          isClosable: true,
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to place bet.",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    }
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <ModalOverlay />
      <ModalContent>
        <ModalHeader textAlign="center">
          <Flex justify="space-between" align="center">
            <Stack align="center" ml="10">
              <Text fontWeight="bold" fontSize="lg">
                {teamA}
              </Text>
              <Text fontSize="sm">Odds: {odds1.toFixed(2)}%</Text>
            </Stack>
            <Text fontSize="2xl" fontWeight="bold" color="red.500">
              VS
            </Text>
            <Stack align="center" mr="10">
              <Text fontWeight="bold" fontSize="lg">
                {teamB}
              </Text>
              <Text fontSize="sm">Odds: {odds2.toFixed(2)}%</Text>
            </Stack>
          </Flex>
        </ModalHeader>
        <ModalCloseButton />
        <ModalBody>
          <Flex direction="column" gap={4}>
            <Flex align="center" justify="space-between">
              <Text>Bet Player</Text>
              <Select
                value={selectedPlayer}
                onChange={(e) => setSelectedPlayer(e.target.value)}
                width="60%"
              >
                <option value="TEAM_A">{teamA}</option>
                <option value="TEAM_B">{teamB}</option>
              </Select>
            </Flex>
            <Flex align="center" justify="space-between">
              <Text>Bet Amount</Text>
              <Input
                placeholder="Enter amount"
                value={betAmount}
                onChange={(e) => setBetAmount(e.target.value)}
                width="60%"
              />
            </Flex>
            <Text>
              Expected Reward: 🪙{" "}
              {expectedWin > 0 ? (expectedWin / 100).toFixed(2) : "0.00"}
            </Text>
          </Flex>
        </ModalBody>
        <ModalFooter>
          <Button variant="outline" mr={3} onClick={onClose}>
            Cancel
          </Button>
          <Button colorScheme="teal" onClick={handleConfirm}>
            Confirm
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  );
};

export default BetModal;
