"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "../../../../config/axiosInstance";
import useAuth from "../../../../config/useAuth";
import LoadingOverlay from "../../../components/loadingOverlay";
import {
  Box,
  Text,
  Flex,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  useToast,
  Heading,
  Tooltip,
  Button,
  Modal,
  ModalOverlay,
  ModalBody,
  ModalContent,
  VStack,
  Spinner
} from "@chakra-ui/react";
import { FaArrowCircleLeft } from "react-icons/fa";
import { TbTournament } from "react-icons/tb";
import { CgPokemon } from "react-icons/cg";
import PlayerMatchComponent from "@/components/playerMatchComponent";
import PokemonDataCard from "@/components/pokemonDataCard";

// format date
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

const TournamentDetails = ({ params }) => {
  const router = useRouter();
  const toast = useToast();
  const [tournament, setTournament] = useState(null);
  const [tournamentData, setTournamentData] = useState(null);
  const [teamData, setTeamData] = useState([]);
  const [isTeamModalOpen, setIsTeamModalOpen] = useState(false);
  const [isLoadingTeam, setIsLoadingTeam] = useState(false);
  
  const { tournamentId } = params;

  // Fetch tournament data on page load
  useEffect(() => {
    const fetchTournamentData = async () => {
      try{
        const response = await axios.get("/player/tournament/me", {
          params: { page: 0, limit: 100 },
        });
        const tournaments = response.data.tournaments;
        
        if (Array.isArray(tournaments)) {
          const tournament = tournaments.find((t) => t.id === tournamentId);
          if (tournament) {
            setTournament(tournament);
          } 
        } else {
          console.error("No tournament data found or data is not an array");
        }
      }
      catch(err){
        console.error("Error fetching tournaments:", err);
      }
  
      // Fetch tournament match data
      try {
        const response = await axios.get(`/tournament/match/${tournamentId}`);
        if (response.status === 200) {
          setTournamentData(response.data);
          console.log("response.data: ", response.data);
          
        } else {
          console.error("Failed to fetch matches: Invalid status code");
        }
      } catch (error) {
        console.error("Error fetching matches: ", error);
        setTournamentData(null); 
      }
    };
  
    fetchTournamentData();
  }, [tournamentId]);

   // Function to fetch the player's team
  const fetchTeamData = async () => {
    setIsLoadingTeam(true);
    try {
      const response = await axios.get(`/player/tournament/${tournamentId}/me/team/`);
      console.log(response.data.chosenPokemons);
      setTeamData(response.data.chosenPokemons);
    } catch (error) {
      console.error("Error fetching team data:", error);
      toast({
        title: "Error",
        description: "Failed to load team data.",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setIsLoadingTeam(false);
    }
  };

  const handleViewTeam = () => { 
    fetchTeamData(); 
    setIsTeamModalOpen(true);
  };

  const handleViewBracket = (tournamentId) => { 
    router.push(`/tournament-bracket/${tournamentId}`);
  };

  const handleBackNavigation = () => {
    router.push("/find-tournament");
  };

  return (
    <Box minH="90vh" bg="white">
      <Flex align={"center"} margin={"1% 0% 2% 2%"} position={"relative"}>
        <Flex align={"center"} onClick={handleBackNavigation}>
          <FaArrowCircleLeft size={"4vh"} />
          <Text ml={"1vh"} fontSize={"3xl"}>
            Back
          </Text>
        </Flex>

        {tournament && (
          <Text
            fontSize="2xl"
            fontWeight="bold"
            position="absolute"
            left="50%"
            transform="translateX(-50%)"
          >
            {tournament.name}
          </Text>
        )}

        {/* Bracket View Button */}
        {tournament && (
          <Tooltip label="View Tournament Bracket" aria-label="View Tournament Bracket">
            <Button
              leftIcon={<TbTournament size={20} />}
              colorScheme="gray"
              variant="solid"
              onClick={() => handleViewBracket(tournamentId)}
              size="md"
              fontSize="sm"
              left="50%"
              ms={10}
            >
              Bracket View
            </Button>
          </Tooltip>
        )}

        {tournament && (
            <Button
              leftIcon={<CgPokemon size={20} />}
              colorScheme="gray"
              variant="solid"
              onClick={handleViewTeam}
              size="md"
              fontSize="sm"
              left="52%"
            >
              View my Team
            </Button>
        )}
        </Flex>

        {tournament && (
          <Text fontSize="xl" textAlign="center">
            {formatDate(tournament.estimatedTournamentPeriod.tournamentBegin)}{" "}
            - {formatDate(tournament.estimatedTournamentPeriod.tournamentEnd)}
          </Text>
        )}

      {/* tournament data and info */}
      <Box m={"0% 3%"}>
        <Tabs variant="enclosed" colorScheme="teal">
          <TabList>
            {tournamentData &&
              tournamentData.map((round, index) => (
                <Tab key={index}>{round.title}</Tab>
              ))}
          </TabList>

          <TabPanels>
            {tournamentData &&
              tournamentData.map((round, index) => (
                <TabPanel key={index} p={4}>
                  {round.seeds.map((seed, seedIndex) => (
                    <PlayerMatchComponent
                      key={seedIndex}
                      seed={seed}
                      toast={toast}
                    />
                  ))}
                </TabPanel>
              ))}
          </TabPanels>
        </Tabs>
      </Box>

      {/* Team Modal */}
      <Modal isOpen={isTeamModalOpen} onClose={() => setIsTeamModalOpen(false)} size="full" isCentered>
        <ModalOverlay />
        <ModalContent maxW="70vw" maxH="80vh" overflowY="auto" borderRadius="lg">
          <Flex justify="center" align="center" p={4} position="relative">
            <Heading size="lg" textAlign="center">
              My Team
            </Heading>
            <Button onClick={() => setIsTeamModalOpen(false)} variant="ghost" size="lg" position="absolute" right="16px">
              ✕
            </Button>
          </Flex>

          <ModalBody>
            {isLoadingTeam ? (
              <VStack align="center">
                <Spinner size="xl" />
                <Text>Loading team data...</Text>
              </VStack>
            ) : (
              <Flex gap={3} wrap="wrap" justify="center" maxW="100%" mx="auto">
                {teamData.length > 0 ? (
                  teamData.map((pokemonData, index) => (
                    <Box key={index} width="calc(33.33% - 16px)" mb={3}>
                      <PokemonDataCard pokemonData={pokemonData} />
                    </Box>
                  ))
                ) : (
                  <Text>No Pokémon in your team.</Text>
                )}
              </Flex>
            )}
          </ModalBody>
        </ModalContent>
      </Modal>
    </Box>
  );
};

export default TournamentDetails;