"use client";
import {
    Box,
    Button,
    Text,
    VStack,
    Modal,
    ModalOverlay,
    ModalContent,
    ModalHeader,
    ModalCloseButton,
    ModalBody,
    ModalFooter
} from '@chakra-ui/react';

function formatDate(dateString) {
    const date = new Date(dateString);

    const options = {
        day: "numeric",
        month: "short",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
        hour12: true,
        timeZone: "Asia/Singapore",
    };

    return new Intl.DateTimeFormat("en-GB", options).format(date);
}

const MatchInfoModal = ({ isOpen, onClose, matchData }) => {
    return (
        <Modal isOpen={isOpen} onClose={onClose} size="md">
            <ModalOverlay />
            <ModalContent>
                <ModalHeader>Match Details</ModalHeader>
                <ModalCloseButton />
                <ModalBody>
                    <VStack align="start" spacing={3}>
                        <Box>
                            <Text fontWeight="bold">Match Time:</Text>
                            <Text>{formatDate(matchData.match.timeMatchOccurs)}</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Match Result:</Text>
                            <Text>{matchData.match.matchResult}</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Match Result Recorded At:</Text>
                            <Text>{formatDate(matchData.match.matchResultRecordedAt)}</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Change in Points (Team A):</Text>
                            <Text>{Math.round(matchData.match.changeInPointsTeamA * 100) / 100}</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Change in Points (Team B):</Text>
                            <Text>{Math.round(matchData.match.changeInPointsTeamB * 100) / 100}</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Team A Win Rate:</Text>
                            <Text>{Math.round(matchData.match.teams[0].winRate * 10000) / 100}%</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Team B Win Rate:</Text>
                            <Text>{Math.round(matchData.match.teams[1].winRate * 10000) / 100}%</Text>
                        </Box>
                        <Box>
                            <Text fontWeight="bold">Forfeited:</Text>
                            <Text>{matchData.match.forfeited ? "Yes" : "No"}</Text>
                        </Box>
                    </VStack>
                </ModalBody>
                <ModalFooter>
                    <Button colorScheme="blue" onClick={onClose}>
                        Close
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
};

export default MatchInfoModal;