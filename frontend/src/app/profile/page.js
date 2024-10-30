"use client";

import { useEffect, useState, useContext } from "react";
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
  IconButton,
} from "@chakra-ui/react";
import { EmailIcon, EditIcon } from "@chakra-ui/icons";

const ProfilePage = () => {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [userInfo, setUserInfo] = useState(null);

  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, user, loading);
  
  // Retrieve username from LocalStorage
  useEffect(() => {
    if (typeof window !== "undefined") {
      const storedUsername = localStorage.getItem("username");
      setUsername(storedUsername); // Fallback to "Guest" if username not found
    }
  }, []);
  
  useEffect(() => {
    const fetchUserData = async () => {
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
  }, [isAuthenticated]);
  
  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;
  console.log("User data fetched:", userInfo);

  
  return (
    <Box>
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
                  {userInfo?.username}
                </Heading>

                <Flex align="center" color="gray.600" mb={2}>
                  <EmailIcon mr={2} size={"lg"} />
                  <Text fontSize="lg">
                    {userInfo?.email}
                    </Text>
                </Flex>

                <Text fontSize="lg" color="gray.500" mb={3}>
                  Team{" "}{userInfo?.clan.name}
                </Text>
              </VStack>
            </Flex>

            {/* Action Buttons */}
            <Flex justify="space-between" mb={8} mx={{ base: "5%", lg: "10%" }}>
              <Button
                colorScheme="teal"
                size="lg"
                height={"90px"}
                minW={"250px"}
                onClick={() => router.push("/calendar")}
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
                {userInfo?.description}
              </Text>
            </Box>
          </Box>
        </Container>
      </Flex>
    </Box>
  );
};

export default ProfilePage;