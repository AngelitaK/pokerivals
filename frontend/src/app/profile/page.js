"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import {
  Avatar,
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
import { FaUsers } from "react-icons/fa";
import { TbPokeball } from "react-icons/tb";

const ProfilePage = () => {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [userInfo, setUserInfo] = useState(null);
  
   // Check authentication
   const { isAuthenticated, user, loading } = useAuth("PLAYER");
   console.log(isAuthenticated, user, loading);
  
  useEffect(() => {
    // This effect will run only after `loading` is false
    if (!loading && isAuthenticated) {
      const storedUsername = localStorage.getItem("username");
      setUsername(storedUsername);
    }
  }, [loading, isAuthenticated]);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await axios.get(`/player/${username}`);
        setUserInfo(response.data); // Store fetched data in state
      } catch (error) {
        //means that the user is admin or unauthorized
        console.error("Error fetching user data:", error);
        if(isAuthenticated){
          return <LoadingOverlay />
        }
      }
    };

    if (username) fetchUserData();
  }, [username]);

  if (loading) return <LoadingOverlay />;
  
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

            {/* Clan box */}
              <Box
                bg="#e7e7e7"
                p={4}
                borderRadius="lg"
                textAlign="center"
              >
                <Icon as={FaUsers} boxSize={10} mb={2}/>
                <Text fontWeight="bold" fontSize="xl">
                  Clan
                </Text>
                {/* <Text color="gray.600" fontSize="lg" textTransform="uppercase">{userInfo?.clan.name}</Text> */}
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