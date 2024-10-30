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
import LoadingOverlay from "../../components/loadingOverlay";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import FindTournament from "./../../components/findTournament";

const FindTournamentPage = () => {
  const router = useRouter();

  // Check authentication
  const { isAuthenticated, user, loading } = useAuth(["PLAYER", "ADMIN"]);
  console.log(isAuthenticated, user, loading);
  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;


  return (
    <Box>
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
    </Box>
  );
};

export default FindTournamentPage;