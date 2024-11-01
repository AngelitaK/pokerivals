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
  const [currentUser, setCurrentUser] = useState("Guest");

  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, user, loading);

  //pagination
  const [currentFriendPage, setCurrentFriendPage] = useState(1);
  const [currentSearchPage, setCurrentSearchPage] = useState(1);
  const itemsPerPage = 8;

  // Slice for current page
  const paginatedFriends = friendsList.slice(
    (currentFriendPage - 1) * itemsPerPage,
    currentFriendPage * itemsPerPage
  );
  const paginatedSearchResults = searchResults.slice(
    (currentSearchPage - 1) * itemsPerPage,
    currentSearchPage * itemsPerPage
  );

  // Pagination handlers
  const handleFriendPageChange = (direction) => {
    setCurrentFriendPage((prev) =>
      direction === "next" ? prev + 1 : Math.max(prev - 1, 1)
    );
  };

  const handleSearchPageChange = (direction) => {
    setCurrentSearchPage((prev) =>
      direction === "next" ? prev + 1 : Math.max(prev - 1, 1)
    );
  };

  useEffect(() => {
    setCurrentUser(localStorage.getItem("username"));

    // Fetch friends list from backend
    const fetchFriendsList = async () => {
      try {
        const response = await axios.get("/player/me/friend", {
          params: { page: 0, limit: 10 },
        });
        setFriendsList(response.data.players);
        console.log("Friends list fetched:", response.data.players);
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
      setSearchResults((prevUsers) =>
        prevUsers.filter((u) => u.username !== user.username)
      ); // Remove from users
    } catch (error) {
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
      setFriendsList((prevFriendsList) =>
        prevFriendsList.filter((friend) => friend.username !== user.username)
      );
    } catch (error) {
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
      const response = await axios.get("/player", {
        params: { query: username, page: 0, limit: 10 },
      });
      setSearchResults(response.data.players);
      console.log("Search results:", response.data);
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
      py={8}
      direction="column"
    >
      <Button
        onClick={() => router.back()}
        colorScheme="blue"
        mb={10}
        alignSelf="flex-start"
      >
        Back
      </Button>

      <Flex w="100%" h="100%" minH="70vh" gap={8} justify="space-between">
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
            {paginatedFriends.length > 0 ? (
              paginatedFriends.map((friend, index) => (
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

          {/* Pagination Controls */}
          <Flex justify="space-between" mt={4}>
            <Button
              size="sm"
              onClick={() => handleFriendPageChange("prev")}
              disabled={currentFriendPage === 1}
            >
              Previous
            </Button>
            <Button
              size="sm"
              onClick={() => handleFriendPageChange("next")}
              disabled={paginatedFriends.length < itemsPerPage}
            >
              Next
            </Button>
          </Flex>
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
            {paginatedSearchResults.length > 0 ? (
              paginatedSearchResults.map((friend, index) => {
                const friendUsernames = friendsList.map(
                  (friend) => friend.username
                );
                const isCurrentUser = friend.username === currentUser; // Check if the friend is the current user
                const isAlreadyFriend = friendUsernames.includes(
                  friend.username
                ); // Check if the friend is already added

                return (
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

                    {!isCurrentUser && !isAlreadyFriend && (
                      <Button
                        colorScheme="teal"
                        size="sm"
                        leftIcon={<AddIcon />}
                        onClick={() => handleAddFriend(friend)}
                      >
                        Add
                      </Button>
                    )}
                  </HStack>
                );
              })
            ) : (
              <Text fontSize="lg">No users found</Text>
            )}
          </VStack>
          {/* Pagination Controls */}
          <Flex justify="space-between" mt={4}>
            <Button
              size="sm"
              onClick={() => handleSearchPageChange("prev")}
              disabled={currentSearchPage === 1}
            >
              Previous
            </Button>
            <Button
              size="sm"
              onClick={() => handleSearchPageChange("next")}
              disabled={paginatedSearchResults.length < itemsPerPage}
            >
              Next
            </Button>
          </Flex>
        </Box>
      </Flex>
    </Flex>
  );
};

export default FriendsPage;
