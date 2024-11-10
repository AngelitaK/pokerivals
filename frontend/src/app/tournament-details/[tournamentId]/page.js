"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "../../../../config/axiosInstance";
import useAuth from "../../../../config/useAuth";
import LoadingOverlay from "../../../components/loadingOverlay";
import {
  Box,
  Text,
  Flex,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  useToast,
  Heading,
} from "@chakra-ui/react";
import { FaArrowCircleLeft } from "react-icons/fa";
import PlayerMatchComponent from "@/components/playerMatchComponent";
import test_data from "./test-data"; //remove once get tournament id

// format date
function formatDate(dateString) {
  const date = new Date(dateString);
  const options = {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "numeric",
    hour12: true,
    timeZone: "Asia/Singapore",
  };
  return new Intl.DateTimeFormat("en-GB", options).format(date);
}

const TournamentDetails = ({ params }) => {
  const router = useRouter();
  const toast = useToast();
  const [tournament, setTournament] = useState(null);
  const [tournamentData, setTournamentData] = useState(null);
  
  let { tournamentId } = params;
  console.log(tournamentId);

  // Fetch tournament data on page load
  useEffect(() => {
    const tournaments = JSON.parse(sessionStorage.getItem("tournaments"));
    console.log(tournaments);

    if (Array.isArray(tournaments)) {
      for (const t of tournaments) {
        if (t.id === tournamentId) {
          setTournament(t);
        }
      }
    } else {
      console.error("No tournament data found or data is not an array");
    }

    //get tournament data from ID
    try {
      const response = axios.get(`/tournament/match/${tournamentId}`);
      console.log(response.data);

      if (response.status !== 200) {
        throw new Error("Failed to fetch matches");
      }

      setTournamentData(response.data); //change when get tournament id
    } catch (error) {
      console.error("Error fetching matches: ", error);
    }

    // setTournamentData(test_data.data); //remove once get tournament id
  }, []);

  const handleViewBracket = (tournamentId) => { 
    router.push(`/tournament-bracket/${tournamentId}`);
  };

  const handleBackNavigation = () => {
    router.push("/manage-tournament");
  };

  return (
    <Box minH="90vh" bg="white">
      <Flex align={"center"} margin={"1% 0% 2% 2%"} position={"relative"}>
        <Flex align={"center"} onClick={handleBackNavigation}>
          <FaArrowCircleLeft size={"4vh"} />
          <Text ml={"1vh"} fontSize={"3xl"}>
            Back
          </Text>
        </Flex>

        {/* tournament name */}
        {tournament && (
          <Flex
            direction={"column"}
            margin={"auto"}
            left={"50%"}
            transform={"translateX(-20%)"}
          >
            <Text fontSize={"2xl"} margin={"auto"} fontWeight={"bold"}>
              {tournament.name}
            </Text>
            <Text fontSize={"xl"} margin={"auto"}>
              {formatDate(tournament.estimatedTournamentPeriod.tournamentBegin)}{" "}
              - {formatDate(tournament.estimatedTournamentPeriod.tournamentEnd)}
            </Text>
          </Flex>
        )}
      </Flex>

      {/* tournament data and info */}
      <Box m={"0% 3%"}>
        <Tabs variant="enclosed" colorScheme="teal">
          <TabList>
            {tournamentData &&
              tournamentData.map((round, index) => (
                <Tab key={index}>{round.title}</Tab>
              ))}
          </TabList>

          <TabPanels>
            {tournamentData &&
              tournamentData.map((round, index) => (
                <TabPanel key={index} p={4}>
                  {round.seeds.map((seed, seedIndex) => (
                    <PlayerMatchComponent
                      key={seedIndex}
                      seed={seed}
                      toast={toast}
                    />
                  ))}
                </TabPanel>
              ))}
          </TabPanels>
        </Tabs>
      </Box>
    </Box>
  );
};

export default TournamentDetails;