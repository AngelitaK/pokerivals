"use client"; 

import React, { useState, useEffect } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam, SeedTime } from 'react-brackets';
import { Box, Text, Heading, useDisclosure, Modal, ModalOverlay, ModalContent, ModalHeader, ModalCloseButton, ModalBody, Flex, Tag, TagLabel, Stack, Divider,  } from '@chakra-ui/react';
import rounds from './completedTournament';
// import axios from '../../../config/axiosInstance'

const tournamentId = "3fa85f64-5717-4562-b3fc-2c963f66afa6"; // Replace the hardcoded tournament ID

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
    <Seed mobileBreakpoint={breakpoint} style={{ width: '0%', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <SeedItem style={{ width: '100%' }}>
        <Box boxShadow="md" bg={seed.forfeited ? "red.600" : "black"}>
          {seed.teams[0]?.id && (
            <Box
              bg={seed.forfeited ? "red.600" : "blue.600"}
              p={2}
              cursor="pointer"
              onClick={() => handlePlayerClick(seed.teams[0])}
              _hover={{ bg: seed.forfeited ? "red.500" : "blue.400" }}
              color={'white'}
              width="100%"
              textAlign="center"
            >
              <SeedTeam fontSize="lg" fontWeight="bold">
                ⚔️ {seed.teams[0].id}
                <Tag ml={2} size="sm" colorScheme={seed.forfeited ? "red" : "blue"} borderRadius="full">
                  <TagLabel>{seed.teams[0].score.toFixed(1)}</TagLabel>
                </Tag> 
              </SeedTeam>
            </Box>
          )}
          <Box height="2px" bg="gray.500"></Box>
          {seed.teams[1]?.id && (
            <Box
              bg={seed.forfeited ? "red.600" : "gray.400"}
              p={2}
              cursor="pointer"
              onClick={() => handlePlayerClick(seed.teams[1])}
              _hover={{ bg: seed.forfeited ? "red.500" : "gray.200" }}
              color={seed.forfeited ? "white" : "black"}
              width="100%"
              textAlign="center"
            >
              <SeedTeam fontSize="lg" fontWeight="bold" color="black">
                ⚔️ {seed.teams[1].id}
                <Tag ml={2} size="sm" colorScheme={seed.forfeited ? "red" : "gray"} borderRadius="full">
                  <TagLabel>{seed.teams[1].score.toFixed(1)}</TagLabel>
                </Tag>
              </SeedTeam>
            </Box>
          )}
        </Box>
      </SeedItem>
      <SeedTime
        mobileBreakpoint={breakpoint}
        fontSize="xs"
        color="gray.600"
        mt="2"
        textAlign="center"
      >
        <Text as="span" color={seed.matchResult === "TEAM_A" ? "green.600" : "green.600"} fontWeight="semibold">
        {seed.matchResult === "TEAM_A"
          ? "Team A Wins!"
          : seed.matchResult === "TEAM_B"
          ? "Team B Wins!"
          : seed.matchResult}
        </Text>
        <Text as="span" mx="2" color="gray.500">
          |
        </Text>
        <Text as="span" color="red.500">
          {formattedDate}
        </Text>
      </SeedTime>

    </Seed>
  );
};

const TournamentPage = () => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [selectedSeed, setSelectedSeed] = useState(null);
  // const [rounds, setRounds] = useState([]);
  // const [loading, setLoading] = useState(true);

  // useEffect(() => {
  //   // Fetch the tournament matches when the component mounts
  //   const fetchMatches = async () => {
  //     try {
  //       const response = await axios.get(`/tournament/match/${TOURNAMENT_ID}`);
  //       setRounds(response.data);
  //       setLoading(false);
  //     } catch (error) {
  //       console.error("Error fetching tournament matches:", error);
  //       setLoading(false);
  //     }
  //   };

  //   fetchMatches();
  // }, []);

  // if (loading) return <Text>Loading tournament data...</Text>;

  const finalRound = rounds.length > 0 ? rounds[rounds.length - 1] : null;
  const finalMatch = finalRound && finalRound.seeds && finalRound.seeds.length > 0 ? finalRound.seeds[0] : null;
  const winner = finalMatch && finalMatch.matchResult === "TEAM_A" ? finalMatch.teams[0].id : finalMatch?.teams[1]?.id;

  const handleSeedClick = (seed) => {
    setSelectedSeed(seed);
    onOpen();
  };

  return (
    <Box padding="20px" bg="gray.50" minHeight="100vh" display="flex" flexDirection="column" alignItems="center">
      <Heading as="h1" textAlign="center" color="teal.700" mb="2" fontSize="3xl">
        Pokémon Tournament Bracket
      </Heading>
      {/* <Box mt={2} mb={6}>
        <Text fontSize="2xl" color="teal.800" fontWeight="bold">
          🎉 Winner: {winner}! 🎉
        </Text>
      </Box> */}
      <Box width="100%" mb={4}>
        <Bracket
          mobileBreakpoint={1000}
          rounds={rounds}
          renderSeedComponent={(props) => <RenderSeed {...props} onSeedClick={handleSeedClick} />}
          swipeableProps={{ enableMouseEvents: true, animateHeight: true }}
        />
      </Box>

      {isOpen && selectedSeed && (
        <Modal isOpen={isOpen} onClose={onClose} isCentered>
          <ModalOverlay />
          <ModalContent boxShadow="2xl" p={4} bg="white" border="2px solid" borderColor="gray.300" borderRadius="md">
            <ModalHeader
              textAlign="center"
              fontWeight="bold"
              fontSize="2xl"
              color="black"
            >
              Player Details
            </ModalHeader>
            <ModalCloseButton border="2px solid" borderColor="black" color="black" />
            <ModalBody>
              <Flex justify="center" mb={6}>
                <Box
                  border="2px solid"
                  borderColor="yellow.400"
                  p={4}
                  bg="yellow.50"
                  w="fit-content"
                  borderRadius="md"
                >
                  <Text fontSize="lg" fontWeight="bold" color="black" textAlign="center">
                    Selected Player:
                    <Text as="span" color="black" fontWeight="semibold"> {selectedSeed?.selectedTeam?.id}</Text>
                  </Text>
                </Box>
              </Flex>

              <Stack spacing={4}>
                <Box>
                  <Text fontSize="md" fontWeight="semibold" color="gray.700">
                    ELO Rating:
                    <Tag ml={2} colorScheme="green" size="md" borderRadius="full">
                      <TagLabel>{selectedSeed?.selectedTeam?.score.toFixed(2)}</TagLabel>
                    </Tag>
                  </Text>
                </Box>

                <Divider />

                <Box>
                  <Text fontSize="md" fontWeight="semibold" color="gray.700">
                    Match Result:
                    <Text as="span" color="teal.600" fontWeight="bold" ml={1}>
                    {selectedSeed.matchResult === "TEAM_A"
                      ? "Team A Wins!"
                      : selectedSeed.matchResult === "TEAM_B"
                      ? "Team B Wins!"
                      : selectedSeed.matchResult}
                    </Text>
                  </Text>
                </Box>

                <Divider />

                <Box>
                  <Text fontSize="md" fontWeight="semibold" color="gray.700">
                    Date:
                    <Text as="span" color="gray.600" fontWeight="medium" ml={1}>
                      {new Date(selectedSeed?.matchResultRecordedAt).toLocaleDateString("en-GB")}
                    </Text>
                  </Text>
                </Box>

                <Divider />

                <Box>
                  <Text fontSize="md" fontWeight="semibold" color="gray.700">
                    Forfeited:
                    <Text as="span" color={selectedSeed?.forfeited ? "red.500" : "black"} ml={1} fontWeight="bold">
                      {selectedSeed?.forfeited ? "Yes" : "No"}
                    </Text>
                  </Text>
                </Box>
              </Stack>
            </ModalBody>
          </ModalContent>
        </Modal>
      )}

    </Box>
  );
};

export default TournamentPage;
