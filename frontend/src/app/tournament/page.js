"use client"; 

import React, { useState } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam, SeedTime } from 'react-brackets';
import { Box, Text, Heading, useDisclosure } from '@chakra-ui/react';
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
            <SeedTeam
              fontSize="lg"
              color="white"
              fontWeight="bold"
              onClick={() => handlePlayerClick(seed.teams[0])}
              cursor="pointer"
              _hover={{ color: "gray.400" }}
            >
              {seed.teams[0]?.id || '-----------'} ({seed.teams[0]?.score.toFixed(1)})
            </SeedTeam>
            <Box height="1px" bg="gray.500" my="2"></Box>
            <SeedTeam
              fontSize="lg"
              color="white"
              fontWeight="bold"
              onClick={() => handlePlayerClick(seed.teams[1])}
              cursor="pointer"
              _hover={{ color: "gray.400" }}
            >
              {seed.teams[1]?.id || '-----------'} ({seed.teams[1]?.score.toFixed(1)})
            </SeedTeam>
          </Box>
        </SeedItem>
        <SeedTime mobileBreakpoint={breakpoint} fontSize="xs" color="gray.400">
          Result: {seed.matchResult} | Date: {formattedDate}
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
      <Box padding="20px" bg="gray.50" minHeight="100vh">
        <Heading as="h1" textAlign="center" color="teal.700" mb="8">
          Pokémon Tournament Bracket
        </Heading>
        <Bracket
          mobileBreakpoint={767}
          rounds={rounds}
          renderSeedComponent={(props) => <RenderSeed {...props} onSeedClick={handleSeedClick} />}
          swipeableProps={{ enableMouseEvents: true, animateHeight: true }}
        />
        <Box textAlign="center" mt="10">
          <Heading as="h2" color="teal.700" fontSize="2xl">
            Winner: player12!
          </Heading>
        </Box>

        {/* Modal or other logic to display selected player/team details */}
        {isOpen && selectedSeed && (
          <Box>
            {/* Modal content */}
            <Text fontSize="lg">Selected Player: {selectedSeed.selectedTeam?.id}</Text>
            <Text>Score: {selectedSeed.selectedTeam?.score}</Text>
            {/* Add more details as needed */}
          </Box>
        )}
      </Box>
    );
  };

  export default TournamentPage;