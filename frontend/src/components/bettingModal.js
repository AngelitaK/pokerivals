"use client"
import { useEffect, useState } from "react";
import { Flex, Text, Button, Stack, Modal, ModalOverlay, ModalContent, ModalHeader, ModalFooter, ModalBody, ModalCloseButton, Select, Input } from "@chakra-ui/react";
import axios from "../../config/axiosInstance";

const BetModal = ({ isOpen, onClose, details }) => {
    const teamA = details.teams[0].id
    const teamB = details.teams[1].id
    const odds1 = details.teams[0].winRate
    const odds2 = details.teams[1].winRate

    const [selectedPlayer, setSelectedPlayer] = useState(teamA);
    const [betAmount, setBetAmount] = useState("");
    const [expectedWin, setExpectedWin] = useState(0);

    useEffect(() => {
        const response = axios.get(`/transaction/betting/bet?matchId=%7B%22tournamentId%22%3A%22${details.tournamentId}%22%2C%22depth%22%3A${details.depth}%2C%22index%22%3A${details.index}%7D&betAmountInCents=${betAmount}&side=${selectedPlayer}`)

        if (response.status !== 200) {
            throw new Error(`Failed to fetch data`);
        }

        const data = response.data
        setExpectedWin(data.winAmountInCents);
    })

    const handleConfirm = async () => {
        try {
            const requestBody = {
                matchId: {
                    tournamentId: details.tournamentId, // Pass the tournament ID dynamically
                    depth: details.depth,
                    index: details.index
                },
                betAmountInCents: parseInt(betAmount) * 100, // Assuming betAmount is in dollars, convert to cents
                side: selectedPlayer
            };

            const response = await axios.post('/transaction/betting/bet', requestBody);

            if (response.status === 200) {
                console.log("Bet placed successfully:", response.data);
                // Handle the successful response here, e.g., show a success message or update the UI
            } else {
                console.error("Failed to place bet:", response.statusText);
                // Handle error response here, e.g., show an error message
            }
        } catch (error) {
            console.error("Error placing bet:", error);
            // Handle network or other errors here
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
                            <Text fontWeight="bold" fontSize="lg">{teamA}</Text>
                            <Text fontSize="sm">Odds: {odds1}</Text>
                        </Stack>
                        <Text fontSize="2xl" fontWeight="bold" color="red.500">VS</Text>
                        <Stack align="center" mr="10">
                            <Text fontWeight="bold" fontSize="lg">{teamB}</Text>
                            <Text fontSize="sm">Odds: {odds2}</Text>
                        </Stack>
                    </Flex>
                </ModalHeader>
                <ModalCloseButton />
                <ModalBody>
                    <Flex direction="column" gap={4}>
                        <Flex align="center" justify="space-between">
                            <Text>Bet Player</Text>
                            <Select value={selectedPlayer} onChange={(e) => setSelectedPlayer(e.target.value)} width="60%">
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
                        <Text>Expected Reward: 🪙{expectedWin}</Text>
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