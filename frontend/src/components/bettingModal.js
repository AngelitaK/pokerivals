"use client"
import { useState } from "react";
import { Flex, Text, Button, Stack, Modal, ModalOverlay, ModalContent, ModalHeader, ModalFooter, ModalBody, ModalCloseButton, Select, Input } from "@chakra-ui/react";
import axios from "../../config/axiosInstance";

const BetModal = ({ isOpen, onClose, player1, player2, odds1, odds2, matchId }) => {
    const [selectedPlayer, setSelectedPlayer] = useState(player1);
    const [betAmount, setBetAmount] = useState("");

    const handleConfirm = async () => {
        try {
            const requestBody = {
                matchId: {
                    tournamentId: matchId.tournamentId, // Pass the tournament ID dynamically
                    depth: matchId.depth,
                    index: matchId.index
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
                            <Text fontWeight="bold" fontSize="lg">{player1}</Text>
                            <Text fontSize="sm">Odds: {odds1}</Text>
                        </Stack>
                        <Text fontSize="2xl" fontWeight="bold" color="red.500">VS</Text>
                        <Stack align="center" mr="10">
                            <Text fontWeight="bold" fontSize="lg">{player2}</Text>
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
                                <option value={player1}>{player1}</option>
                                <option value={player2}>{player2}</option>
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