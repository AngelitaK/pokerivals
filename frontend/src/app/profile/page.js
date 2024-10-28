"use client";

import { useEffect, useState, useContext } from "react";
import { UserContext } from "../Providers.js";
import { useRouter } from "next/navigation";
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "axios";
import {
  Avatar,
  Badge,
  Box,
  Button,
  Container,
  Flex,
  Heading,
  Stack,
  Text,
  VStack,
  IconButton,
  useToast,
} from "@chakra-ui/react";
import { EmailIcon, EditIcon } from "@chakra-ui/icons";

const ProfilePage = () => {
  const [users, setUsers] = useState([]); // Store remaining users
  const [friends, setFriends] = useState([]); // Store friends
  const { userData } = useContext(UserContext);
  const toast = useToast();
  const [isUser, setIsUser] = useState(false); // User authentication state
  const [loading, setLoading] = useState(true); // Loading state
  const router = useRouter();

  // Check for session ID on component mount
  useEffect(() => {
    const checkSession = async () => {
      try {
        setLoading(true); // Show loading overlay

        //  verify session with an API call
        const response = await axios.get("http://localhost:8080/test", {
          withCredentials: true,
          headers: {
            "Content-Type": "application/json",
          },
        });
        console.log("Session check response:", response.data);

        if (response.data.role != null) {
          setIsUser(true); // Set user state to true if authenticated
        } else {
          router.push("/login"); // Redirect if not authenticated
        }
      } catch (error) {
        console.error("Session check failed:", error);
        router.push("/login"); // Redirect on error
      } finally {
        setLoading(false); // Hide loading overlay after check
      }
    };

    checkSession();
  }, [router]);

  /**
   * Transfer these functions to separate pages
   */

  // // Fetch friends from the backend on load
  // useEffect(() => {
  //   const fetchFriends = async () => {
  //     try {
  //       const response = await fetch("http://localhost:8080/player/me/friend", {
  //         method: "GET",
  //         credentials: "include", // Maintain session
  //         headers: {
  //           "Content-Type": "application/json",
  //         },
  //       });

  //       if (!response.ok) {
  //         throw new Error("Failed to fetch friends");
  //       }

  //       const data = await response.json();
  //       setFriends(data); // Set the friend list
  //     } catch (error) {
  //       console.error("Error fetching friends:", error);
  //     }
  //   };

  //   fetchFriends();
  // }, []);

  /**
   * Transfer these functions to separate pages
   */

  // // Add friend to backend
  // const handleAddFriend = async (user) => {
  //   try {
  //     const response = await fetch(
  //       `http://localhost:8080/player/me/friend/${user.username}`,
  //       {
  //         method: "POST",
  //         credentials: "include", // Maintain session
  //         headers: {
  //           "Content-Type": "application/json",
  //         },
  //       }
  //     );

  //     if (!response.ok) {
  //       throw new Error(`Failed to add ${user.username} as a friend`);
  //     }

  //     const data = await response.json();

  //     // Show success toast
  //     toast({
  //       title: "Friend Added",
  //       description: `${user.username} added to your friends!`,
  //       status: "success",
  //       duration: 9000,
  //       isClosable: true,
  //     });

  //     // Update the state
  //     setFriends([...friends, user]);
  //     setUsers(users.filter((u) => u.username !== user.username)); // Remove from users
  //   } catch (error) {
  //     console.error("Error adding friend:", error);
  //     toast({
  //       title: "Error",
  //       description: `Failed to add ${user.username} as a friend.`,
  //       status: "error",
  //       duration: 9000,
  //       isClosable: true,
  //     });
  //   }
  // };

  // // Delete friend from backend
  // const handleDeleteFriend = async (user) => {
  //   try {
  //     const response = await fetch(
  //       `http://localhost:8080/player/me/friend/${user.username}`,
  //       {
  //         method: "DELETE",
  //         credentials: "include", // Maintain session
  //         headers: {
  //           "Content-Type": "application/json",
  //         },
  //       }
  //     );

  //     if (!response.ok) {
  //       throw new Error(`Failed to remove ${user.username} from friends`);
  //     }

  //     const data = await response.json();

  //     // Show success toast
  //     toast({
  //       title: "Friend Removed",
  //       description: `${user.username} has been removed from your friends!`,
  //       status: "success",
  //       duration: 9000,
  //       isClosable: true,
  //     });

  //     // Update the state to remove the friend from the friends list
  //     setFriends(friends.filter((friend) => friend.username !== user.username));
  //   } catch (error) {
  //     console.error("Error removing friend:", error);
  //     toast({
  //       title: "Error",
  //       description: `Failed to remove ${user.username} from friends.`,
  //       status: "error",
  //       duration: 9000,
  //       isClosable: true,
  //     });
  //   }
  // };

  // // Function to fetch users based on search term
  // const handleSearch = async (query) => {
  //   if (query.length === 0) {
  //     setUsers([]); // Clear the user list if the search is empty
  //     return;
  //   }

  //   try {
  //     const response = await fetch(
  //       `http://localhost:8080/player?query=${query}`,
  //       {
  //         method: "GET",
  //         credentials: "include",
  //         headers: {
  //           "Content-Type": "application/json",
  //         },
  //       }
  //     );

  //     if (!response.ok) {
  //       throw new Error("Failed to search users");
  //     }

  //     const data = await response.json();
  //     setUsers(data); // Update the users list with search results
  //   } catch (error) {
  //     console.error("Error fetching users:", error);
  //   }
  // };

  return (
    <Box>
      {/* Show loading overlay when checking session */}
      {loading && <LoadingOverlay />}

      {/* Render main content only if not loading and user is authenticated */}
      {!loading && isUser && (
        <>
          <Flex
            minH="100vh"
            bgImage="/TopupBG.png"
            bgSize="cover"
            bgPosition="center"
            align="center"
            justify="center"
            p={5}
          >
            <Container maxW="100%" p={0}>
              <Box
                maxW="100%"
                maxH="100%"
                minH="85vh"
                bg="rgba(255, 255, 255, 0.95)"
                borderRadius="xl"
                boxShadow="lg"
                p={"5%"}
                mx="auto"
                textAlign="center"
              >
                {/* Top Section: Avatar on the left, User Info on the right */}
                <Flex align="center" mb={"5%"}>
                  <Avatar size="2xl" name="username" mr={6} />

                  <VStack align="flex-start" spacing={1} flex="1">
                    <Flex justify="flex-end" w="100%">
                      <IconButton
                        icon={<EditIcon />}
                        aria-label="Edit profile"
                        variant="ghost"
                        size="lg"
                      />
                    </Flex>

                    <Heading size="xl" fontWeight="bold" mb={2}>
                      Baddie4U
                    </Heading>

                    <Flex align="center" color="gray.600" mb={2}>
                      <EmailIcon mr={2} size={"lg"} />
                      <Text fontSize="lg">dabaddest@hotmail.com</Text>
                    </Flex>

                    <Text fontSize="lg" color="gray.500" mb={3}>
                      Team Rocket
                    </Text>
                  </VStack>
                </Flex>

                {/* Action Buttons */}
                <Flex
                  justify="space-between"
                  mb={8}
                  mx={{ base: "5%", lg: "10%" }}
                >
                  <Button
                    colorScheme="teal"
                    size="lg"
                    height={"90px"}
                    minW={"250px"}
                    onClick={() => router.push("/view-calendar")}
                  >
                    View Calendar
                  </Button>
                  <Button
                    colorScheme="teal"
                    size="lg"
                    height={"90px"}
                    minW={"250px"}
                    onClick={() => router.push("/view-friends")}
                  >
                    View Friends List
                  </Button>
                  <Button
                    colorScheme="teal"
                    size="lg"
                    height={"90px"}
                    minW={"250px"}
                    onClick={() => router.push("/find-tournament")}
                  >
                    Find Tournament
                  </Button>
                </Flex>

                {/* About Me Section */}
                <Box textAlign="left" mt={"5%"}>
                  <Heading size="lg" mb={5}>
                    About Me
                  </Heading>
                  <Text fontSize="lg" color="gray.700">
                    Passionate Pokémon trainer on a journey to become a Pokémon
                    Master. Always up for a battle or trading rare Pokémon.
                    Let's connect and share our adventures!
                  </Text>
                </Box>
              </Box>
            </Container>
          </Flex>
        </>
      )}
    </Box>
  );
};

export default ProfilePage;