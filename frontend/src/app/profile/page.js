  "use client";

  import { useState, useContext, useEffect } from "react";
  import { UserContext } from "../Providers.js";
  import {
    Avatar,
    Box,
    Flex,
    Tabs,
    TabList,
    TabPanels,
    Tab,
    TabPanel,
    Stack,
    Button,
    Heading,
    Text,
    Grid,
    HStack,
    VStack,
    IconButton,
    useToast
  } from "@chakra-ui/react";
  import { AddIcon, DeleteIcon } from '@chakra-ui/icons';
  import SearchBar from "@/components/searchBar";

  const FriendItem = ({ name, username, onAddFriend }) => {
    return (
      <Box maxW="90%" mx="auto">
        <Flex align="center" justify="space-between" mt={5}>
          <HStack>
            <Avatar />
            <VStack align="start">
              <Text color="black" fontWeight="bold">{name}</Text>
              <Text fontSize="sm" color="gray.500">{username}</Text>
            </VStack>
          </HStack>
          <IconButton 
            icon={<AddIcon />} 
            aria-label="Add Friend" 
            onClick={onAddFriend} // Attach the onClick event
          />
        </Flex>
      </Box>
    );
  };

  const UserItem = ({ name, username, onDeleteFriend }) => {
    return (
      <Box maxW="90%" mx="auto">
        <Flex align="center" justify="space-between" mt={5}>
          <HStack>
            <Avatar />
            <VStack align="start">
              <Text color="black" fontWeight="bold">{name}</Text>
              <Text fontSize="sm" color="gray.500">{username}</Text>
            </VStack>
          </HStack>
          <IconButton 
            icon={<DeleteIcon/>} 
            aria-label="Remove Friend" 
            onClick={onDeleteFriend}
          />
        </Flex>
      </Box>
    )
  }

  const ProfilePage = () => {
    const [users, setUsers] = useState([]); // Store remaining users
    const [friends, setFriends] = useState([]); // Store friends
    const { userData } = useContext(UserContext);
    const toast = useToast();

    // Fetch friends from the backend on load
    useEffect(() => {
      const fetchFriends = async () => {
        try {
          const response = await fetch("http://localhost:8080/player/me/friend", {
            method: "GET",
            credentials: "include", // Maintain session
            headers: {
              "Content-Type": "application/json",
            },
          });
          
          if (!response.ok) {
            throw new Error("Failed to fetch friends");
          }

          const data = await response.json();
          setFriends(data); // Set the friend list
        } catch (error) {
          console.error("Error fetching friends:", error);
        }
      };

      fetchFriends();
    }, []);

    // Add friend to backend
    const handleAddFriend = async (user) => {
      try {
        const response = await fetch(`http://localhost:8080/player/me/friend/${user.username}`, {
          method: "POST",
          credentials: "include", // Maintain session
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error(`Failed to add ${user.username} as a friend`);
        }

        const data = await response.json();

        // Show success toast
        toast({
          title: "Friend Added",
          description: `${user.username} added to your friends!`,
          status: "success",
          duration: 9000,
          isClosable: true,
        });

        // Update the state
        setFriends([...friends, user]);
        setUsers(users.filter((u) => u.username !== user.username)); // Remove from users

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

    // Delete friend from backend
    const handleDeleteFriend = async (user) => {
      try {
        const response = await fetch(`http://localhost:8080/player/me/friend/${user.username}`, {
          method: "DELETE",
          credentials: "include", // Maintain session
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error(`Failed to remove ${user.username} from friends`);
        }

        const data = await response.json();

        // Show success toast
        toast({
          title: "Friend Removed",
          description: `${user.username} has been removed from your friends!`,
          status: "success",
          duration: 9000,
          isClosable: true,
        });

        // Update the state to remove the friend from the friends list
        setFriends(friends.filter((friend) => friend.username !== user.username));

      } catch (error) {
        console.error("Error removing friend:", error);
        toast({
          title: "Error",
          description: `Failed to remove ${user.username} from friends.`,
          status: "error",
          duration: 9000,
          isClosable: true,
        });
      }
    };


    // Function to fetch users based on search term
    const handleSearch = async (query) => {
      if (query.length === 0) {
        setUsers([]); // Clear the user list if the search is empty
        return;
      }
      
      try {
        const response = await fetch(`http://localhost:8080/player?query=${query}`, {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error("Failed to search users");
        }

        const data = await response.json();
        setUsers(data); // Update the users list with search results
      } catch (error) {
        console.error("Error fetching users:", error);
      }
    };

    return (
      <Stack
        minH={"100vh"}
        bgImage="/TopupBG.png"
        bgSize="cover"
        bgPosition="center"
      >
        <Flex p="50px">
          <Grid templateColumns="repeat(2, 1fr)" gap={6} w="100%" h="100%">
            <Flex
              bg="rgba(255, 255, 255, 0.8)"
              p={10}
              borderRadius="2xl"
              boxShadow="md"
              mb={6}
              gap={3}
              direction="column"
              justify="center"  
              align="center"
              color="black"
            >
              <Avatar size="2xl" name="John Doe" />
              <Flex align="center" mt={4}>
                <Heading size="lg" fontWeight="bold">
                  {userData?.username || 'John Doe'}
                </Heading>
              </Flex>

              <Text mt={5} fontSize="xl">
                {userData?.description || 'No description available'}
              </Text>
              <Text mt={5} fontSize="xl">
                Tournaments won: {userData?.tournamentsWon || 0}
              </Text>
              <Text fontSize="xl">Points: {userData?.points || 0}</Text>

              <Text fontSize="xl" fontWeight="bold">
                Clan: {userData?.clan?.name || 'No clan'}
              </Text>

              <Stack spacing={6} mt={8} direction="row">
                <Button colorScheme="teal" size="lg">
                  Save
                </Button>
                <Button variant="outline" size="lg">
                  Cancel
                </Button>
              </Stack>
            </Flex>

            <Flex
              bg="rgba(255, 255, 255, 0.8)"
              p={10}
              borderRadius="2xl"
              boxShadow="md"
              mb={6}
              gap={3}
              direction="column"
            >
              <Tabs size="md" variant="enclosed">
                <TabList>
                  <Tab
                  _selected={{ color: "white", bg: "blue.500" }} 
                  _hover={{ color: "black" }}
                  color="black"
                  >
                    Add Friends
                  </Tab>
                  <Tab
                  _selected={{ color: "white", bg: "blue.500" }} 
                  _hover={{ color: "black" }}
                  color="black"
                  >
                    Friend Lists
                  </Tab>
                </TabList>
                <TabPanels>
                  {/* Add Friends Tab */}
                  <TabPanel>
                    <Box mb={4}>
                      <SearchBar handleSearch={handleSearch} />
                    </Box>
                    <Box
                      maxH="400px" 
                      overflowY="auto"
                    >
                      {users.map((user, index) => (
                        <FriendItem
                          key={index}
                          name={user.name}
                          username={user.username}
                          onAddFriend={() => handleAddFriend(user)} // Add the user when clicked
                        />
                      ))}
                    </Box>
                  </TabPanel>

                  {/* Friend List Tab */}
                  <TabPanel>
                    <Box
                        maxH="400px" 
                        overflowY="auto"
                      >
                        {friends.map((user, index) => (
                          <UserItem
                            key={index}
                            name={user.name}
                            username={user.username}
                            onDeleteFriend={() => handleDeleteFriend(user)}
                          />
                        ))}
                      </Box>
                  </TabPanel>
                </TabPanels>
              </Tabs>

            </Flex>
          </Grid>
        </Flex>
      </Stack>
    );
  };

  export default ProfilePage;
