"use client"
import { Flex, Text, Button, Icon, Stack } from "@chakra-ui/react";
import { useState } from "react";
import BetModal from "./bettingModal";

const BettingComponent = ({ details }) => {
    const teamA = details.teams[0].id
    const teamB = details.teams[1].id
    const odds1 = details.teams[0].winRate
    const odds2 = details.teams[1].winRate

    const [isBetModalOpen, setBetModalOpen] = useState(false);
    const [selectedMatch, setSelectedMatch] = useState(null);

    const openBetModal = () => {
        console.log(details)
        setSelectedMatch(details);
        setBetModalOpen(true);
    };

    const closeBetModal = () => {
        setBetModalOpen(false);
        setSelectedMatch(null);
    };

    return (
        <Flex
            bg="whiteAlpha.800"
            borderRadius="md"
            p={4}
            align="center"
            justify="space-between"
            mb={4}
            boxShadow="lg"
            border="1px solid"
            borderColor="gray.300"
            minH="100px"
            _hover={{ bg: "whiteAlpha.900", transform: "scale(1.02)", transition: "0.2s" }}
            transition="all 0.2s ease-in-out"
        >
            <Stack spacing={1} align="center" width="20%">
                <Text fontWeight="bold" fontSize="lg" color="gray.700">{teamA}</Text>
                <Text fontSize="sm" color="gray.600">Odds: {odds1}</Text>
            </Stack>

            <Text fontWeight="bold" fontSize="2xl" color="red.500">
                VS
            </Text>

            <Stack spacing={1} align="center" width="20%">
                <Text fontWeight="bold" fontSize="lg" color="gray.700">{teamB}</Text>
                <Text fontSize="sm" color="gray.600">Odds: {odds2}</Text>
            </Stack>

            <Button colorScheme="teal" size="md" onClick={openBetModal}>Bet</Button>

            {selectedMatch && (
                <BetModal
                    isOpen={isBetModalOpen}
                    onClose={closeBetModal}
                    details={selectedMatch}
                />
            )}
        </Flex>
    );
};

export default BettingComponent;