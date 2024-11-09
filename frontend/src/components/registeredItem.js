import React from 'react';
import { Box, Flex, HStack, VStack, Text, Button } from '@chakra-ui/react';

const RegisteredItem = ({ tournament, buttonLabel, onButtonClick, onTournamentClick, isDisabled }) => {
    const {
        name,
        estimatedTournamentPeriod,
        eloLimit,
    } = tournament;

    // Format dates to a more readable format
    const formatDateTime = (dateString) => {
        return new Date(dateString).toLocaleString();
    };

    return (
        <Box 
            bg="rgba(255, 255, 255, 0.9)" 
            borderRadius="lg" 
            boxShadow="lg" 
            p={4}  
            minW="100%" 
            onClick={() => onTournamentClick(tournament.id)} // Trigger onTournamentClick with the tournament ID
            cursor="pointer"
            _hover={{ shadow: "xl", transform: "scale(1.02)" }} 
        >
            <Flex align="center" justify="space-between">
                <HStack spacing={4}>
                    <VStack align="start">
                        <Text color="black" fontWeight="bold" fontSize="lg">{name}</Text>
                        <Text fontSize="sm" color="gray.600">{formatDateTime(estimatedTournamentPeriod.tournamentBegin)}</Text>
                        <Text fontSize="sm" color="gray.600">Min Elo: {eloLimit.minElo}</Text>
                        <Text fontSize="sm" color="gray.600">Max Elo: {eloLimit.maxElo}</Text>
                    </VStack>
                </HStack>
                <Button 
                    colorScheme={buttonLabel === "Leave" ? "red" : "green"} 
                    onClick={(e) => {
                        e.stopPropagation(); // Prevent parent onClick when button is clicked
                        onButtonClick();
                    }}
                    disabled={isDisabled}
                >
                    {buttonLabel}
                </Button>
            </Flex>
        </Box>
    );
};

export default RegisteredItem;
