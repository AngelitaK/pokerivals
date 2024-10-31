'use client';

import { useState, useEffect } from 'react';
import {
  Flex,
  Box,
  Stack,
  Button,
  Heading,
  Text,
  Grid,
  Input,
  InputGroup,
  InputRightElement,
} from "@chakra-ui/react";
import { FcCalendar } from "react-icons/fc"; 
import { useRouter } from 'next/navigation'; 
import Link from 'next/link'; 
import TournamentItem from '../../components/tournamentItem';
import axios from '../../../config/axiosInstance'; 
import SearchBar from "@/components/searchBar";


const FindTournamentPage = () => {  
  const [tournaments, setTournaments] = useState([]); 
  const [searchResults, setSearchResults] = useState([]); // Search results
  const [loading, setLoading] = useState(true); // Track loading state
  const [error, setError] = useState(null); // Track errors
  const router = useRouter();

  // Function to fetch all the tournaments the user is in
  useEffect(() => {
    const fetchTournaments = async () => {
      try {
        const response = await axios.get('/player/tournament/me?page=0&limit=10'); 
        setTournaments(response.data.tournaments);
      } catch (err) {
        console.error("Error fetching tournaments:", err);
        setError("Failed to load tournaments."); // Set error message
      } finally {
        setLoading(false); // Set loading to false after fetching
      }
    };

    fetchTournaments();
  }, []); 

  // Function to handle withdrawal from a tournament
  const handleWithdraw = async (tournamentId) => {
    try {
      const response = await axios.delete(`/player/tournament/${tournamentId}/leave`);

      if (response.status === 200) {
        const data = response.data;
        console.log(data.message);
        // Remove the tournament from state
        setTournaments((prevTournaments) => prevTournaments.filter(tournament => tournament.id !== tournamentId));
      }
    } catch (error) {
      console.error("Error leaving tournament:", error);
    }
  };

  // Function to handle search
  const handleSearch = async (query) => {
    if (!query) {
      setSearchResults([]); // Clear search results if no query
      return;
    }

    try {
      const response = await axios.get(`/player/tournament/search`, {
        params: {
          page: 0, // Update to handle pagination if needed
          limit: 3, // Change limit according to the API specification
          query: query // This is the search term
        }
      });
      
      // Check if the response data structure matches your expectation
      if (response.data && response.data.tournaments) {
        setSearchResults(response.data.tournaments); // Set the search results
      } else {
        setSearchResults([]); // Handle unexpected structure
        console.error("Unexpected response structure:", response.data);
      }
    } catch (error) {
      console.error("Error searching tournaments:", error);
      setError("Failed to search tournaments."); // Set error message
    }
  };


  // Joining tournaments will need a Pokemon Team
  const handleJoinTournament = async () => { 
    router.push('/choose-pokemon');    
  };

  return (
    <Stack
      minH={"100vh"}
      bgImage="/TournamentBG.jpg"
      bgSize="cover"
      bgPosition="center"
    >
      <Flex justifyContent="center" alignItems="center" width="100%">
        <Box maxWidth="1200px" width="100%" mt={10} color={"white"}>
          <Heading
            textAlign="center"
            textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
            mb={4}
            sx={{
              WebkitTextStroke: "1px black",  
              color: "white"
            }}
          >
            Find a Tournament
          </Heading>  

          <Flex p="50px">
            <Grid templateColumns="repeat(2, 1fr)" gap={6} w="100%" h="100%">
              {/* Left Flex Box for Registered Tournaments */}
              <Flex
                bg="rgba(255, 255, 255, 0.8)"
                p={10}
                borderRadius="2xl"
                boxShadow="md"
                mb={6}
                gap={3}
                direction="column"
                color="black"
                maxHeight="500px" 
                overflowY="auto"
              >
                <Flex direction="row" gap={5}>
                  <Heading textAlign="center" textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)" mb={4} sx={{ WebkitTextStroke: "1px black",  color: "white"}} size="md">Tournaments Registered</Heading>
                  <Link href="/calendar">
                      <FcCalendar mr={10} size={24} style={{ cursor: 'pointer' }} />
                  </Link>
                </Flex>
                {
                  tournaments.length === 0 ? (
                    <Text>No tournaments registered yet.</Text> // Display message if no tournaments
                  ) : (
                    tournaments.map((tournament) => (
                      <TournamentItem 
                        key={tournament.id} 
                        tournament={tournament} 
                        buttonLabel="Leave"
                        onButtonClick={() => handleWithdraw(tournament.id)} 
                      />
                    ))
                  )
                }
              </Flex>

              {/* Right Flex Box for Joining Tournaments */}
              <Flex
                bg="rgba(255, 255, 255, 0.8)"
                p={10}
                borderRadius="2xl"
                boxShadow="md"
                mb={6}
                gap={3}
                direction="column"
                maxHeight="500px" 
                overflowY="auto"
              >
                <Flex direction="row" gap={5}>
                  <Heading textAlign="center" textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)" mb={4} sx={{ WebkitTextStroke: "1px black",  color: "white"}} size="md">Join Tournament</Heading>
                </Flex>
                <SearchBar handleSearch={handleSearch} /> 
                <Flex direction="column" gap={3}>
                  {searchResults.length === 0 ? (
                    <Text color='black'>No tournaments found.</Text>
                  ) : (
                    searchResults.map((tournament) => (
                      <TournamentItem 
                        key={tournament.id} 
                        tournament={tournament} 
                        buttonLabel="Join"
                        onButtonClick={() => handleJoinTournament(tournament.id)} 
                      />
                    ))
                  )}
                </Flex>
              </Flex>
            </Grid>
          </Flex>               
        </Box>
      </Flex>
    </Stack>
  );
};

export default FindTournamentPage;
