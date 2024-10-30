"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import LoadingOverlay from "@/components/loadingOverlay";
import SearchBar from "@/components/searchBar";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import {
  Box,
  Flex,
  Heading,
  HStack,
  VStack,
  Avatar,
  Text,
  Button,
  Spacer,
  useToast,
} from "@chakra-ui/react";
import { AddIcon } from "@chakra-ui/icons";

const FriendsPage = () => {
  const router = useRouter();
  const toast = useToast();
  const [friendsList, setFriendsList] = useState([]);
  const [searchResults, setSearchResults] = useState([]);

  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, user, loading);

  useEffect(() => {
    // Fetch friends list from backend
    const fetchFriendsList = async () => {
      try {
        const response = await axios.get("/player/me/friend", {
          params: { page: 0, limit: 10 },
        });
        setFriendsList(response.data.players);
        console.log("Friends list fetched:", response.data);
      } catch (error) {
        console.error("Error fetching friends list:", error);
      }
    };

    fetchFriendsList();
  }, []);

  // Add friend to backend
  const handleAddFriend = async (user) => {
    console.log("Adding friend:", user);

    try {
      const response = await axios.post(`/player/me/friend/${user.username}`);
      if (response.status !== 200) {
        throw new Error(`Failed to add ${user.username} as a friend`);
      }
      console.log(response.data);

      // Show success toast
      toast({
        title: "Friend Added",
        description: `${user.username} added to your friends!`,
        status: "success",
        duration: 3000,
        isClosable: true,
      });

      // Update the state
      setFriendsList((prevFriends) => [...prevFriends, user]);
      setSearchResults((prevUsers) => prevUsers.filter((u) => u.username !== user.username)); // Remove from users
    } 
    catch (error) {
      console.error("Error adding friend:", error);
      toast({
        title: "Error",
        description: `Failed to add ${user.username} as a friend.`,
        status: "error",
        duration: 9000,
        isClosable: true,
      });
    }
  };

  // Remove friend from friends list
  const handleDeleteFriend = async (user) => {
    try {
      console.log(user.username);

      const response = await axios.delete(`/player/me/friend/${user.username}`);
      if (response.status !== 200) {
        throw new Error(`Failed to remove ${username} from friends`);
      }
      console.log(response.data);

      // Show success toast
      toast({
        title: "Friend Removed",
        description: `${user.username} has been removed from your friends!`,
        status: "success",
        duration: 9000,
        isClosable: true,
      });

      // Update the state to remove the friend from the friends list
      setFriendsList((prevFriendsList) =>prevFriendsList.filter((friend) => friend.username !== user.username));
    } 
    catch (error) {
      console.error("Error removing friend:", error);
      toast({
        title: "Error",
        description: `Failed to remove ${username} from friends.`,
        status: "error",
        duration: 9000,
        isClosable: true,
      });
    }
  };

  // Handle search input change
  const handleSearch = async (username) => {
    if (!username.trim()) {
      setSearchResults([]); // Clear results if the query is empty
      return;
    }

    try {
      const response = await axios.get(`/player/${username}`);
      const data = response.data;

      // If data is an object, wrap it in an array to ensure consistency
      setSearchResults(Array.isArray(data) ? data : [data]);
      console.log("Search results:", data); // Log results to check data
    } catch (error) {
      console.error("Error fetching users:", error);
    }
  };

  // loading screen
  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;

  return (
    <Flex
      minH="100vh"
      bgImage="/TopupBG.png"
      bgSize="cover"
      bgPosition="center"
      align="center"
      justify="center"
      px={5}
      py={0}
    >
      <Flex w="100%" h="100%" minH="70vh" gap={10} justify="space-between">
        {/* Friends List Section */}
        <Box
          flex="1"
          bg="whiteAlpha.900"
          borderRadius="lg"
          boxShadow="lg"
          p={5}
          overflow="hidden"
        >
          <Heading size="lg" mb={6} color="black">
            Friends List
          </Heading>

          {/* Friend list retrieved from backend */}
          <VStack spacing={4} align="stretch">
            {friendsList.length > 0 ? (
              friendsList.map((friend, index) => (
                <HStack key={index} spacing={4} align="center" w="100%">
                  <Avatar size="md" />
                  <Box>
                    <Text fontWeight="bold" color="black">
                      {friend.username}, Team{" "}
                      {friend.clan && friend.clan.name
                        ? friend.clan.name
                        : "No Clan"}
                    </Text>
                  </Box>
                  <Spacer />

                  <Button
                    colorScheme="red"
                    size="sm"
                    onClick={() => handleDeleteFriend(friend)}
                  >
                    Remove
                  </Button>
                </HStack>
              ))
            ) : (
              <Text fontSize="lg">C'mon, go and make some new friends</Text>
            )}
          </VStack>
        </Box>

        {/* Add Friends Section */}
        <Box
          flex="1"
          bg="whiteAlpha.900"
          borderRadius="lg"
          boxShadow="lg"
          p={5}
          overflow="hidden"
        >
          <Heading size="lg" mb={6} color="black">
            Add Friends
          </Heading>

          {/* search bar */}
          <SearchBar handleSearch={handleSearch} />

          {/* result from friend search */}
          <VStack spacing={4} align="stretch">
            {searchResults.length > 0 ? (
              searchResults.map((friend, index) => (
                <HStack key={index} spacing={4} align="center" w="100%">
                  <Avatar size="md" />
                  <Box>
                    <Text fontWeight="bold" color="black">
                      {friend.username}, Team{" "}
                      {friend.clan && friend.clan.name
                        ? friend.clan.name
                        : "No Clan"}
                    </Text>
                  </Box>
                  <Spacer />

                  <Button
                    colorScheme="teal"
                    size="sm"
                    leftIcon={<AddIcon />}
                    onClick={() => handleAddFriend(friend)}
                  >
                    Add
                  </Button>
                </HStack>
              ))
            ) : (
              <Text fontSize="lg">No users found</Text>
            )}
          </VStack>
        </Box>
      </Flex>
    </Flex>
  );
};

export default FriendsPage;