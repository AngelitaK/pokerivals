import React from 'react';
import { Box, Flex, HStack, VStack, Text, Button } from '@chakra-ui/react';

const RegisteredItem = ({ tournament, buttonLabel, onButtonClick, onTournamentClick, isDisabled }) => {
    const {
        name,
        registrationPeriod,
        estimatedTournamentPeriod,
        eloLimit,
    } = tournament;

    // Format dates to a more readable format
    const formatDateTime = (dateString) => {
        const date = new Date(dateString);
        const day = date.getDate();
        const suffix = day % 10 === 1 && day !== 11 ? 'st' :
                       day % 10 === 2 && day !== 12 ? 'nd' :
                       day % 10 === 3 && day !== 13 ? 'rd' : 'th';
        const options = { month: 'short', hour: 'numeric', minute: 'numeric', hour12: true };
        return `${day}${suffix} ${date.toLocaleString('en-US', options)}`;
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
                        <Text fontSize="sm" color="gray.600">
                            Registration: {formatDateTime(registrationPeriod.registrationBegin)} - {formatDateTime(registrationPeriod.registrationEnd)}
                        </Text>
                        <Text fontSize="sm" color="gray.600">
                            Tournament: {formatDateTime(estimatedTournamentPeriod.tournamentBegin)} - {formatDateTime(estimatedTournamentPeriod.tournamentEnd)}
                        </Text>
                        <Text fontSize="sm" color="gray.600">ELO Range {eloLimit.minElo} - {eloLimit.maxElo}</Text>
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
