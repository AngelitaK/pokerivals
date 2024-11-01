"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import {
  Avatar,
  Badge,
  Box,
  Button,
  Container,
  Flex,
  Heading,
  Text,
  VStack,
  Icon,
  SimpleGrid
} from "@chakra-ui/react";
import { EmailIcon, CalendarIcon } from "@chakra-ui/icons";
import { FaUsers, FaPokeball } from "react-icons/fa";
import { TbPokeball } from "react-icons/tb";

const ProfilePage = () => {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [userInfo, setUserInfo] = useState(null);

  
  // Retrieve username from LocalStorage
  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, user, loading);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const storedUsername = localStorage.getItem("username");
      setUsername(storedUsername);
    }
  }, []);

  
  
  useEffect(() => {
    const fetchUserData = async () => {
      console.log("Username:", username);
      try {
        const response = await axios.get(`/player/${username}`);
        const data = response.data;
        setUserInfo(data); // Store fetched data in state
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    };

    // Only fetch data if authenticated
    if (isAuthenticated) {
      fetchUserData();
    }

    //test
    const fetchTest = async () => {
      try {
      const response = await axios.get('/me');
      const currentUserData = response.data;
      console.log("Current User Data:", currentUserData);
      } catch (error) {
      console.error("Error fetching current user data:", error);
      }
    };

    fetchTest();

  }, [isAuthenticated]);

  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;
  console.log("User data fetched:", userInfo);

  return (
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
          p={10}
          mx="auto"
          textAlign="center"
        >
          {/* Top Section: Avatar on the left, User Info on the right */}
          <Flex align="center" mb={"5%"}>
            <Avatar size="2xl" name={userInfo?.username} mr={6} />

            <VStack align="flex-start" spacing={1} flex="1">
              <Heading size="xl" fontWeight="bold" mb={2}>
                {userInfo?.username}
              </Heading>
             
              <Text fontSize="lg"mb={2} fontWeight="bold">
                Team{" "}{userInfo?.clan.name}
              </Text>

              <Flex align="center" color="gray.600">
                <EmailIcon mr={2} size={"lg"} />
                <Text fontSize="lg">{userInfo?.email}</Text>
              </Flex>
            </VStack>
          </Flex>

          {/* Action Buttons */}
          <SimpleGrid columns={3} spacing={10} mb={2}>
            {/* calendar */}
            <Box
                bg="#e7e7e7"
                p={5}
                borderRadius="lg"
                textAlign="center"
                onClick={() => router.push("/calendar")}
                cursor="pointer"
                 _hover={{ bg: "#d7d7d7" }}
              >
                <Icon as={CalendarIcon} boxSize={10} mb={3}/>
                <Text fontWeight="bold" fontSize="xl">
                  View Calendar
                </Text>
              </Box>

            {/* friends box */}
              <Box
                bg="#e7e7e7"
                p={4}
                borderRadius="lg"
                textAlign="center"
              >
                <Icon as={FaUsers} boxSize={10} mb={2}/>
                <Text fontWeight="bold" fontSize="xl">
                  Friends
                </Text>
                <Text color="gray.600" fontSize="lg">{userInfo?.noOfFriends}</Text>
              </Box>

              {/* points box */}
              <Box
                bg="#e7e7e7"
                p={4}
                borderRadius="lg"
                textAlign="center"
              >
                <Icon as={TbPokeball} boxSize={10} mb={2} />
                <Text fontWeight="bold" fontSize="xl">
                  Points
                </Text>
                <Text color="gray.600" fontSize="lg">{userInfo?.points}</Text>
              </Box>
          </SimpleGrid>

          {/* About Me Section */}
          <Box textAlign="left" my={6}>
            <Heading size="lg" pt={5} mb={5}>
              About Me
            </Heading>
            <Text fontSize="lg" color="gray.700">
              {userInfo?.description}
            </Text>
          </Box>

          {/* BUTTONS BOTTOM  */}
          <Flex justify="space-between" pt={10}>
            <Button
              colorScheme="teal"
              size="lg"
              minW={"180px"}
              onClick={() => router.push("/friend-list")}
            >
              View Friends List
            </Button>
            <Button
              colorScheme="teal"
              size="lg"
              minW={"180px"}
              onClick={() => router.push("/find-tournament")}
            >
              Find Tournament
            </Button>
          </Flex>
        </Box>
      </Container>
    </Flex>
  );
};

export default ProfilePage;