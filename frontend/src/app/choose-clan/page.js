"use client";

import { useEffect, useState, useContext, useMemo} from "react";
import { useRouter } from "next/navigation";
import { useToast } from "@chakra-ui/react";
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import {
  Box,
  Button,
  Flex,
  Heading,
  Image,
  Text,
  VStack,
} from "@chakra-ui/react";

// Array with clan names and logos
const teams = [
  { name: "Rocket", logo: "/rocketlogo.png", bgColor: "#d16363" },
  { name: "Aqua", logo: "/aqualogo.png", bgColor: "#e3eef9" },
  { name: "Magma", logo: "/magmalogo.png", bgColor: "#6a5b65" },
  { name: "Galactic", logo: "/galacticlogo.png", bgColor: "#ffe082" },
];

const TeamSelectionPage = () => {
  const router = useRouter();
  const toast = useToast();
  
  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, user, loading);
  
  // Function to handle the "Join Team" button click
  const handleJoinTeam = async (teamName) => {
    try {
      // PATCH request to add the user to the selected clan
      const response = await axios.patch(`/player/me/clan/${teamName}`,{withCredentials: true,});
      const data = response.data;
      
      // Show a success message
      toast({
        title: "Success",
        description:
        data.message || `You have joined ${teamName} successfully!`,
        status: "success",
        duration: 2000,
        isClosable: true,
      });
      
      // After successfully joining the team, navigate to the profile page
      router.push("/profile");
    } catch (error) {
      console.error("Error joining team:", error);
      
      // Show an error message if something goes wrong
      toast({
        title: "Error",
        description: `Failed to join ${teamName}. Please try again.`,
        status: "error",
        duration: 2000,
        isClosable: true,
      });
    }
  };
  
  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;

  return (
    <Box h="100%" bg="white">
      <Box position="relative" h="100vh">
        <Box
          position="absolute"
          top="10%"
          left="50%"
          transform="translateX(-50%)"
          zIndex="1"
          bg="white"
          px={4} 
          py={2} 
          borderRadius="md" 
          boxShadow="md" 
        >
          <Heading color="black" size="xl">
            Choose Your Team
          </Heading>
        </Box>

        {/* Team Selection Boxes */}
        <Flex h="100%">
          {teams.map((team, index) => (
            <VStack
              key={index}
              bg={team.bgColor}
              w="25%" // Dividing into four sections evenly
              h="100%"
              justify="center"
              spacing={6}
            >
              <Text fontWeight="bold" fontSize="xl" color="black">
                {team.name}
              </Text>
              <Image
                src={team.logo}
                alt={`${team.name} Logo`}
                boxSize="200px"
              />
              {/* Button to join the specific clan */}
              <Button
                onClick={() => handleJoinTeam(team.name)} // This will send the correct clan name
                colorScheme="blue"
              >
                Join Team
              </Button>
            </VStack>
          ))}
        </Flex>
      </Box>
    </Box>
  );
};

export default TeamSelectionPage;