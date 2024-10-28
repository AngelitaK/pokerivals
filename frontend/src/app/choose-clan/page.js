"use client";

import { useEffect, useState, useContext } from "react";
import {
  Box,
  Button,
  Flex,
  Heading,
  Image,
  Text,
  VStack,
} from "@chakra-ui/react";
import { useRouter } from "next/navigation";
import { useToast } from "@chakra-ui/react";
import { UserContext } from "../Providers.js";
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "axios";

// Array with clan names and logos
const teams = [
  { name: "Rocket", logo: "/rocketlogo.png", bgColor: "#d16363" },
  { name: "Aqua", logo: "/aqualogo.png", bgColor: "#e3eef9" },
  { name: "Magma", logo: "/magmalogo.png", bgColor: "#6a5b65" },
  { name: "Galactic", logo: "/galacticlogo.png", bgColor: "#ffe082" },
];

const TeamSelectionPage = () => {
  const [isUser, setIsUser] = useState(false); // User authentication state
  const [loading, setLoading] = useState(true); // Loading state
  const router = useRouter();
  const toast = useToast();
  const { userData, setUserData } = useContext(UserContext);

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

  // Function to handle the "Join Team" button click
  const handleJoinTeam = async (teamName) => {
    try {
      // PATCH request to add the user to the selected clan
      const response = await fetch(
        `http://localhost:8080/player/me/clan/${teamName}`,
        {
          method: "PATCH",
          credentials: "include", // Include credentials to maintain the session
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      // Check if the response is not okay
      if (!response.ok) {
        throw new Error(`Failed to join ${teamName}: ${response.statusText}`);
      }

      // Parse the response data
      const data = await response.json();

      // Update the UserContext with the new clan
      setUserData((prevData) => ({
        ...prevData,
        clan: { name: teamName },
      }));

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

  return (
    <Box h="100%" bg="white">
      <Box position="relative" h="100vh">
        {/* Show loading overlay when checking session */}
        {loading && <LoadingOverlay />}

        {/* Render main content only if not loading and user is authenticated */}
        {!loading && isUser && (
          <>
            <Heading
              position="absolute"
              top="0"
              left="50%"
              transform="translateX(-50%)"
              zIndex="1"
              color="black"
              size="xl"
              mt={4}
            >
              Choose Your Team
            </Heading>

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
                    boxSize="100px"
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
          </>
        )}
      </Box>
    </Box>
  );
};

export default TeamSelectionPage;