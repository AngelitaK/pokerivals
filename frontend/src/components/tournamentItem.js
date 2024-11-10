import React from 'react';
import { Box, Flex, HStack, VStack, Text, Button } from '@chakra-ui/react';

const TournamentItem = ({ tournament, buttonLabel, onButtonClick, isDisabled }) => {
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

    const isPastRegistrationEnd = new Date() > new Date(registrationPeriod.registrationEnd);

    return (
        <Box 
            bg="rgba(255, 255, 255, 0.9)" 
            borderRadius="lg" 
            boxShadow="lg" 
            p={4}  
            minW="100%" 
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
                    colorScheme={isPastRegistrationEnd ? "yellow" : buttonLabel === "Leave" ? "red" : "green"}
                    onClick={onButtonClick}
                    disabled={isDisabled || isPastRegistrationEnd}
                >
                    {isPastRegistrationEnd ? "Closed" : buttonLabel}
                </Button>
            </Flex>
        </Box>
    );
};

export default TournamentItem;
