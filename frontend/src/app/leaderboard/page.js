"use client";

import { useEffect, useState } from "react";
import LoadingOverlay from "@/components/loadingOverlay";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import {
  Box,
  Text,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  VStack,
  Flex,
  Heading,
} from "@chakra-ui/react";
import LeaderBoardMember from "@/components/leaderboardMember";

const LeaderBoard = () => {
  const [globalList, setGlobalList] = useState([]);
  const [teamList, setTeamList] = useState([]);
  const [friendsList, setFriendsList] = useState([]);
  const [clan, setClan] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);

  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, user, loading);

  useEffect(() => {
    const username = localStorage.getItem("username");
    console.log(username);

    // Fetch current user data
    const fetchSelf = async () => {
      try {
        const response = await axios.get(`/player/${username}`, {
          params: { page: 0, limit: 10 },
        });
        console.log("Response data:", response.data);
        setClan(response.data.clan?.name);
        setCurrentUser(response.data);
      } catch (error) {
        console.error("Error fetching self:", error);
      }
    };

    fetchSelf();
  }, []); 

  useEffect(() => {
    // Only fetch rankings if currentUser is set
    if (currentUser) {

      // Fetch global ranking
      const fetchGlobalRanking = async () => {
        try {
          const response = await axios.get("/player/me/non-friend", {
            params: {
              page: 0,
              limit: 10,
            },
          });
          const sortedPlayers = response.data.players.sort(
            (a, b) => b.points - a.points
          );
          setGlobalList(sortedPlayers);
          // console.log("Response data:", sortedPlayers);
        } catch (error) {
          console.error("Error fetching global ranking:", error);
        }
      };

      fetchGlobalRanking();

      // Fetch clan ranking
      const fetchClanRanking = async () => {
        if (!clan) return; // Don't fetch if clan is not yet set

        try {
          const response = await axios.get(
            `/player/clan/${encodeURIComponent(clan)}`,
            {
              params: {
                page: 0,
                limit: 10,
                name: clan,
              },
            }
          );
          console.log("Clan ranking data:", response.data);
          const sortedPlayers = response.data.players.sort(
            (a, b) => b.points - a.points
          );
          setTeamList(sortedPlayers);
        } catch (error) {
          console.error("Error fetching clan ranking:", error);
        }
      };

      fetchClanRanking();

      // Fetch friends ranking
      const fetchFriendsRanking = async () => {
        try {
          const response = await axios.get("/player/me/friend", {
            params: {
              page: 0,
              limit: 10,
            },
          });
          const sortedPlayers = response.data.players.sort(
            (a, b) => b.points - a.points
          );
          setFriendsList([currentUser, ...sortedPlayers]);
          console.log("Friends ranking data:", sortedPlayers);
        } catch (error) {
          console.error("Error fetching friends ranking:", error);
        }
      };

      fetchFriendsRanking();
    }
  }, [currentUser, clan]);


   // loading screen
   if (loading) return <LoadingOverlay />;
   if (!isAuthenticated) return null;


  return (
    <Box
      p={8}
      minH="100vh"
      backgroundImage="url('/leaderboardBG.png')"
      backgroundSize="cover"
      backgroundPosition="center"
      bgopacity="0.8"
    >
      <Heading textAlign="center" pb={6}>
        Leaderboard
      </Heading>

      <Box bg="whiteAlpha.800" borderRadius="md" boxShadow="md" p={4}>
        <Tabs variant="soft-rounded" colorScheme="blue">
          {/* tabs to change list */}
          <TabList justifyContent="center">
            <Tab
              fontWeight="bold"
              fontSize="lg"
              _selected={{ bg: "blue.600", color: "white" }}
            >
              Global Ranking
            </Tab>
            <Tab
              fontWeight="bold"
              fontSize="lg"
              _selected={{ bg: "blue.600", color: "white" }}
            >
              Clan Ranking
            </Tab>
            <Tab
              fontWeight="bold"
              fontSize="lg"
              _selected={{ bg: "blue.600", color: "white" }}
            >
              Friends Ranking
            </Tab>
          </TabList>

          <TabPanels>
            {/* global ranking */}
            <TabPanel>
              <Flex
                fontWeight="bold"
                my={3}
                color="black"
                justify="space-between"
              >
                <Text mx={10} fontSize="xl  ">
                  Player
                </Text>
                <Text mx={10} fontSize="xl">
                  Clan
                </Text>
                <Text mx={10} fontSize="xl">
                  Points
                </Text>
              </Flex>

              <VStack alignItems="stretch" w="100%">
                {Array.isArray(globalList) &&
                  globalList.map((player, index) => (
                    <LeaderBoardMember
                      key={index}
                      player={player.username}
                      clan={player.clan ? player.clan.name : "No clan"}
                      points={player.points}
                    />
                  ))}
              </VStack>
            </TabPanel>

            {/* Clan ranking */}
            <TabPanel>
              <Flex
                fontWeight="bold"
                my={3}
                color="black"
                justify="space-between"
              >
                <Text mx={10} fontSize="xl">
                  Player
                </Text>
                <Text mx={10} fontSize="xl">
                  Clan
                </Text>
                <Text mx={10} fontSize="xl">
                  Points
                </Text>
              </Flex>

              <VStack alignItems="stretch" w="100%">
                {Array.isArray(teamList) &&
                  teamList.map(
                    (player, index) =>
                      player && player.username ? ( 
                        <LeaderBoardMember
                          key={index}
                          player={player.username}
                          clan={player.clan ? player.clan.name : "No clan"}
                          points={player.points}
                        />
                      ) : null // If player or player.username is not valid, render nothing
                  )}
              </VStack>
            </TabPanel>

            {/* Friends ranking */}
            <TabPanel>
              <Flex
                fontWeight="bold"
                my={3}
                color="black"
                justify="space-between"
              >
                <Text mx={10} fontSize="xl">
                  Player
                </Text>
                <Text mx={10} fontSize="xl">
                  Clan
                </Text>
                <Text mx={10} fontSize="xl">
                  Points
                </Text>
              </Flex>

              <VStack alignItems="stretch" w="100%">
                {Array.isArray(friendsList) &&
                  friendsList.map(
                    (player, index) =>
                      player && player.username ? ( 
                        <LeaderBoardMember
                          key={index}
                          player={player.username}
                          clan={player.clan ? player.clan.name : "No clan"}
                          points={player.points}
                        />
                      ) : null // If player or player.username is not valid, render nothing
                  )}
              </VStack>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </Box>
    </Box>
  );
};

export default LeaderBoard;
