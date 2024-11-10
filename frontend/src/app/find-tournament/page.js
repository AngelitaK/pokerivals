'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Flex,
  Box,
  Stack,
  Button,
  Heading,
  Text,
  Grid,
  useToast
} from "@chakra-ui/react";
import { FcCalendar } from "react-icons/fc";
import { useRouter } from 'next/navigation'; 
import Link from 'next/link';
import TournamentItem from '../../components/tournamentItem';
import axios from '../../../config/axiosInstance';
import SearchBar from "@/components/searchBar";
import RegisteredItem from './../../components/registeredItem';

const FindTournamentPage = () => {  
  const [tournaments, setTournaments] = useState([]); 
  const [userId, setUserId] = useState();
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [tournamentPage, setTournamentPage] = useState(0);
  const [query, setQuery] = useState('');
  const [hasMoreResults, setHasMoreResults] = useState(true);
  const [totalRegisteredTournaments, setTotalRegisteredTournaments] = useState(0); // Total tournaments count
  const limit = 3;
  const tournamentLimit = 4;
  const router = useRouter();
  const toast = useToast();

  useEffect(() => {
    // This runs only on the client
    const userId = localStorage.getItem("username");
    if (userId) {
      setUserId(userId);
    }
  }, []);


  // Function to fetch all the tournaments the user is registered in
  const fetchTournaments = useCallback(async (newPage = 0) => {
    setTournamentPage(newPage);
    setLoading(true);

    try {
      const response = await axios.get('/player/tournament/me', {
        params: {
          page: newPage,
          limit: tournamentLimit
        }
      });
      
      setTournaments(response.data.tournaments);
      setTotalRegisteredTournaments(response.data.count); // Set the total tournament count
    } catch (err) {
      console.error("Error fetching tournaments:", err);
      setError("Failed to load tournaments.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTournaments(); // Fetch tournaments on component mount
  }, [fetchTournaments]);

  // Calculate if there are more pages based on count and limit
  const totalPages = Math.ceil(totalRegisteredTournaments / tournamentLimit);
  const hasMoreTournaments = tournamentPage < totalPages - 1;

  // Handle pagination for registered tournaments
  const handleNextTournamentPage = () => {
    if (hasMoreTournaments) {
      fetchTournaments(tournamentPage + 1);
    }
  };
  const handlePreviousTournamentPage = () => fetchTournaments(Math.max(tournamentPage - 1, 0));

  const handleWithdraw = async (tournamentId) => {
    try {
      const response = await axios.delete(`/player/tournament/${tournamentId}/leave`);
      if (response.status === 200) {
        setTournaments((prevTournaments) => prevTournaments.filter(tournament => tournament.id !== tournamentId));
        setTotalRegisteredTournaments((prevTotal) => prevTotal - 1); // Adjust total after withdraw
      }
    } catch (error) {
      console.error("Error leaving tournament:", error);
    }
  };

  const handleSearch = useCallback(async (searchQuery, newPage = 0) => {
    setQuery(searchQuery);
    setPage(newPage);
    setLoading(true);

    if (!searchQuery) {
      setSearchResults([]);
      setLoading(false);
      return;
    }

    try {
      const response = await axios.get(`/player/tournament/search`, {
        params: {
          page: newPage,
          limit: limit,
          query: searchQuery
        }
      });
      
      if (response.data && response.data.tournaments) {
        setSearchResults(response.data.tournaments);
        setHasMoreResults(response.data.tournaments.length === limit);
      } else {
        setSearchResults([]);
        console.error("Unexpected response structure:", response.data);
      }
    } catch (error) {
      console.error("Error searching tournaments:", error);
      setError("Failed to search tournaments.");
    } finally {
      setLoading(false);
    }
  }, []);

  const handleNextPage = () => handleSearch(query, page + 1);
  const handlePreviousPage = () => handleSearch(query, Math.max(page - 1, 0));

  useEffect(() => {
    if (query) {
      handleSearch(query, page);
    }
  }, [page, query, handleSearch]);

  // Check if user is invited before attempting to join
  const handleJoinTournament = (tournament) => {

    console.log(userId)

    if (!tournament.invited_players?.includes(userId)) {
      toast({
        title: "Not Invited",
        description: "You are not invited to join this tournament.",
        status: "error",
        duration: 4000,
        isClosable: true,
      });
    } else {
      router.push(`/choose-pokemon/${tournament.id}`);
    }
  };

  const handleTournamentClick = (tournamentId) => {
    router.push(`/tournament-details/${tournamentId}`);
  };

  return (
    <Stack minH={"100vh"} bgImage="/TournamentBG.jpg" bgSize="cover" bgPosition="center">
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
                    <Text>No tournaments registered yet.</Text>
                  ) : (
                    tournaments.map((tournament) => {
                      const isRegistrationEnded = new Date() > new Date(tournament.registrationPeriod.registrationEnd);
                      return (
                        <RegisteredItem
                          key={tournament.id} 
                          tournament={tournament} 
                          buttonLabel="Leave"
                          onButtonClick={() => handleWithdraw(tournament.id)}
                          isDisabled={isRegistrationEnded}
                          onTournamentClick={handleTournamentClick}
                        />
                      );
                    })
                  )
                }
                {/* Pagination Controls - Only show if there are tournaments */}
                {tournaments.length > 0 && (
                  <Flex justifyContent="space-between" mt={4}>
                    <Button onClick={handlePreviousTournamentPage} isDisabled={tournamentPage === 0}>Previous</Button>
                    <Button onClick={handleNextTournamentPage} isDisabled={!hasMoreTournaments}>Next</Button>
                  </Flex>
                )}
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
                <SearchBar handleSearch={(query) => handleSearch(query, 0)} /> 
                <Flex direction="column" gap={3}>
                  {searchResults.length === 0 ? (
                    <Text color='black'>No tournaments found.</Text>
                  ) : (
                    searchResults.map((tournament) => (
                      <TournamentItem 
                        key={tournament.id} 
                        tournament={tournament} 
                        buttonLabel="Join"
                        onButtonClick={() => handleJoinTournament(tournament)} 
                      />
                    ))
                  )}
                </Flex>

                {/* Pagination Controls - Only show if there are search results */}
                {searchResults.length > 0 && (
                  <Flex justifyContent="space-between" mt={4}>
                    <Button onClick={handlePreviousPage} isDisabled={page === 0}>Previous</Button>
                    <Button onClick={handleNextPage} isDisabled={!hasMoreResults}>Next</Button>
                  </Flex>
                )}
              </Flex>
            </Grid>
          </Flex>               
        </Box>
      </Flex>
    </Stack>
  );
};

export default FindTournamentPage;