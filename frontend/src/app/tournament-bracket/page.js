"use client"; 

import React, { useState } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam, SeedTime } from 'react-brackets';
import { Box, Text, Heading, useDisclosure, Modal, ModalOverlay, ModalContent, ModalHeader, ModalCloseButton, ModalBody } from '@chakra-ui/react';
// Test tournament data
// import rounds from './tournamentData';

// Test complete tournament data
// import rounds from './completedTournament';

// Test incomplete tournament data
  import rounds from './incompleteTournament';

  const RenderSeed = ({ breakpoint, seed, onSeedClick }) => {
    const formattedDate = new Date(seed.matchResultRecordedAt).toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });

    const handlePlayerClick = (team) => {
      onSeedClick({ ...seed, selectedTeam: team });
    };

    return (
      <Seed mobileBreakpoint={breakpoint}>
        <SeedItem style={{ width: '100%' }}>
          <Box padding="10px" bg={seed.forfeited ? "red.600" : "black"} borderRadius="md" boxShadow="md">
            {seed.teams[0]?.id && (
              <SeedTeam
                fontSize="lg"
                color="white"
                fontWeight="bold"
                onClick={() => handlePlayerClick(seed.teams[0])}
                cursor="pointer"
                _hover={{ color: "gray.400" }}
              >
                {seed.teams[0].id} ({seed.teams[0].score.toFixed(1)})
              </SeedTeam>
            )}
            <Box height="1px" bg="gray.500" my="2"></Box>
              {seed.teams[1]?.id && (
              <SeedTeam
                fontSize="lg"
                color="white"
                fontWeight="bold"
                onClick={() => handlePlayerClick(seed.teams[1])}
                cursor="pointer"
                _hover={{ color: "gray.400" }}
              >
                {seed.teams[1].id} ({seed.teams[1].score.toFixed(1)})
              </SeedTeam>
            )}
          </Box>
        </SeedItem>
        <SeedTime mobileBreakpoint={breakpoint} fontSize="xs" color="gray.400">
          {seed.matchResult} | Date: {formattedDate}
        </SeedTime>
      </Seed>
    );
  };

  const TournamentPage = () => {
    const { isOpen, onOpen, onClose } = useDisclosure();
    const [selectedSeed, setSelectedSeed] = useState(null);

    const handleSeedClick = (seed) => {
      setSelectedSeed(seed);
      onOpen();
    };

    return (
      <Box padding="20px" bg="gray.50" minHeight="100vh" display="flex" flexDirection="column" alignItems="center">
        <Heading as="h1" textAlign="center" color="teal.700" mb="8" fontSize="3xl">
          Pokémon Tournament Bracket
        </Heading>
        <Box width="100%">
          <Bracket
            mobileBreakpoint={767}
            rounds={rounds}
            renderSeedComponent={(props) => <RenderSeed {...props} onSeedClick={handleSeedClick} />}
            swipeableProps={{ enableMouseEvents: true, animateHeight: true }}
          />
        </Box>
        <Box textAlign="center" mt="10">
          <Heading as="h2" color="teal.700" fontSize="2xl">
            Winner: player12!
          </Heading>
        </Box>

        {isOpen && selectedSeed && (
          <Modal isOpen={isOpen} onClose={onClose} isCentered>
            <ModalOverlay />
            <ModalContent>
              <ModalHeader>Player Details</ModalHeader>
              <ModalCloseButton />
              <ModalBody>
                <Text fontSize="lg">Selected Player: {selectedSeed?.selectedTeam?.id}</Text>
                <Text>Score: {selectedSeed?.selectedTeam?.score}</Text>
                <Text>Match Result: {selectedSeed?.matchResult}</Text>
                <Text>Date: {new Date(selectedSeed?.matchResultRecordedAt).toLocaleDateString("en-GB")}</Text>
                <Text>Forfeited: {selectedSeed?.forfeited ? "Yes" : "No"}</Text>
              </ModalBody>
            </ModalContent>
          </Modal>
        )}
      </Box>
    );
  };

  export default TournamentPage;