"use client";

import React, { useState } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam, SeedTime } from 'react-brackets';
import { Box, Text, Heading, useDisclosure, Modal, ModalOverlay, ModalContent, ModalHeader, ModalCloseButton, ModalBody, ModalFooter, Button } from '@chakra-ui/react';
import initialRounds from '../tournament-bracket/incompleteTournament';

// Initialize rounds so only the first round has player data, all others start empty
const initializeRounds = (rounds) => {
  return rounds.map((round, index) => {
    if (index === 0) return round;
    return {
      ...round,
      seeds: round.seeds.map(seed => ({
        ...seed,
        teams: [{ id: "", score: 0, empty: true }, { id: "", score: 0, empty: true }],
        matchResult: "PENDING",
      })),
    };
  });
};

// Component to render each Seed
const RenderSeed = ({ breakpoint, seed, onPlayerClick }) => {
  const formattedDate = new Date(seed.matchResultRecordedAt).toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });

  return (
    <Seed mobileBreakpoint={breakpoint}>
      <SeedItem style={{ width: '100%' }}>
        <Box padding="10px" bg={seed.forfeited ? "red.600" : "black"} borderRadius="md" boxShadow="md">
          {seed.teams[0]?.id && (
            <SeedTeam
              fontSize="lg"
              color="white"
              fontWeight="bold"
              onClick={() => onPlayerClick(seed, seed.teams[0])}
              cursor="pointer"
              _hover={{ color: "gray.400" }}
              bg="gray.700"
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
              onClick={() => onPlayerClick(seed, seed.teams[1])}
              cursor="pointer"
              _hover={{ color: "gray.400" }}
              bg="blackAlpha.900"
            >
              {seed.teams[1].id} ({seed.teams[1].score.toFixed(1)})
            </SeedTeam>
          )}
        </Box>
      </SeedItem>
      <SeedTime mobileBreakpoint={breakpoint} fontSize="xs" color="gray.400">
        {seed.matchResult !== "PENDING" ? seed.matchResult : "PENDING"} | Date: {formattedDate}
      </SeedTime>
    </Seed>
  );
};

// Main Tournament Page Component
const TournamentPage = () => {
  const [rounds, setRounds] = useState(initializeRounds(initialRounds));
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [selectedSeed, setSelectedSeed] = useState(null);
  const [selectedPlayer, setSelectedPlayer] = useState(null);

  const handlePlayerClick = (seed, player) => {
    // Store the seed and player details for confirmation
    setSelectedSeed(seed);
    setSelectedPlayer(player);
    onOpen();
  };

  const confirmResult = () => {
    const updatedRounds = JSON.parse(JSON.stringify(rounds)); // Deep copy to prevent direct mutation
    const currentRoundIndex = updatedRounds.findIndex(round => round.seeds.some(s => s.index === selectedSeed.index));
    const currentSeedIndex = updatedRounds[currentRoundIndex].seeds.findIndex(s => s.index === selectedSeed.index);

    // Set match result in the current round's seed
    updatedRounds[currentRoundIndex].seeds[currentSeedIndex].matchResult = selectedPlayer.id;

    // Advance the player to the next round
    if (currentRoundIndex + 1 < updatedRounds.length) {
      const nextRound = updatedRounds[currentRoundIndex + 1];
      const nextSeedIndex = Math.floor(currentSeedIndex / 2);
      const teamPosition = currentSeedIndex % 2 === 0 ? 0 : 1;

      // Update the next round's team with the selected player
      if (nextRound.seeds[nextSeedIndex]) {
        nextRound.seeds[nextSeedIndex].teams[teamPosition] = {
          ...selectedPlayer,
          empty: false,
        };
      }
    }

    // Update state with modified rounds and close modal
    setRounds(updatedRounds);
    onClose();
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
          renderSeedComponent={(props) => <RenderSeed {...props} onPlayerClick={handlePlayerClick} />}
          swipeableProps={{ enableMouseEvents: true, animateHeight: true }}
        />
      </Box>
      <Box textAlign="center" mt="10">
        {rounds[rounds.length - 1].seeds[0].teams[0].id ? (
          <Heading as="h2" color="teal.700" fontSize="2xl">
            Winner: {rounds[rounds.length - 1].seeds[0].teams[0].id}!
          </Heading>
        ) : (
          <Heading as="h2" color="teal.700" fontSize="2xl">
            Tournament in Progress
          </Heading>
        )}
      </Box>

      {isOpen && selectedSeed && (
        <Modal isOpen={isOpen} onClose={onClose} isCentered>
          <ModalOverlay />
          <ModalContent>
            <ModalHeader>Confirm Result</ModalHeader>
            <ModalCloseButton />
            <ModalBody>
              <Text fontSize="lg">Confirm {selectedPlayer.id} as the winner for this match?</Text>
            </ModalBody>
            <ModalFooter>
              <Button colorScheme="green" mr={3} onClick={confirmResult}>
                Yes
              </Button>
              <Button variant="ghost" onClick={onClose}>
                No
              </Button>
            </ModalFooter>
          </ModalContent>
        </Modal>
      )}
    </Box>
  );
};

export default TournamentPage;
