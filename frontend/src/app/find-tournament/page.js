"use client";
import { useEffect, useState, useContext } from "react";
import {
  Center,
  Flex,
  Box,
  Stack,
  Button,
  Heading,
  Text,
  Grid,
  GridItem,
  Image,
} from "@chakra-ui/react";
import { useRouter } from "next/navigation";
import { UserContext } from "../Providers.js";
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "axios";
import FindTournament from "./../../components/findTournament";

const FindTournamentPage = () => {
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

  return (
    <Box>
      {/* Show loading overlay when checking session */}
      {loading && <LoadingOverlay />}

      {/* Render main content only if not loading and user is authenticated */}
      {!loading && isUser && (
        <>
          <Stack
            minH={"100vh"}
            bgImage="/TournamentBG.jpg"
            bgSize="cover"
            bgPosition="center"
          >
            {/* Match results for pokemon sets used */}
            <Flex justifyContent="center" alignItems="center" width="100%">
              <Box maxWidth="1200px" width="100%" mt={10} color={"white"}>
                <Heading
                  textAlign="center"
                  textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
                  mb={4}
                  sx={{
                    WebkitTextStroke: "1px black",
                    color: "white",
                  }}
                >
                  Find a Tournament
                </Heading>

                <Flex fontWeight="bold" mb={5} mt={5} color="white">
                  <Text ml={10} mr={20} fontSize="xl">
                    Player Name
                  </Text>
                  <Text ml={20} mr={20} fontSize="xl">
                    Date
                  </Text>
                  <Text ml={40} mr={20} fontSize="xl">
                    Time
                  </Text>
                  <Text ml={10} fontSize="xl">
                    Min. Rating
                  </Text>
                  <Text ml={10} fontSize="xl">
                    Max.Rating
                  </Text>
                </Flex>

                <FindTournament />
              </Box>
            </Flex>
          </Stack>
        </>
      )}
    </Box>
  );
};

export default FindTournamentPage;