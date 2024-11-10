"use client";
import React, { useEffect, useState } from "react";
import axios from "../../../../config/axiosInstance";
import { useRouter, useSearchParams } from "next/navigation";
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
} from "@chakra-ui/react";
import { FaArrowCircleLeft } from "react-icons/fa";
import MatchComponent from "@/components/matchComponent";

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

const ManageMatchesPage = () => {
  const router = useRouter();
  const [tournament, setTournament] = useState(null);
  const [tournamentData, setTournamentData] = useState(null);
  const toast = useToast();

  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  // Fetch tournament data on page load
  useEffect(() => {
    const fetchTournamentData = async () => {
      const tournaments = JSON.parse(sessionStorage.getItem("tournaments"));
      for (var t of tournaments) {
        if (t.id == id) {
          setTournament(t);
        }
      }

      try {
        const response = await axios.get(`/tournament/match/${id}`);

        if (response.status !== 200) {
          throw new Error("Failed to fetch matches");
        }

        setTournamentData(response.data);

      } catch (error) {
        console.error("Error fetching matches: ", error)
      }
      fetchTournamentData();
    }}, [id]);

    const handleBackNavigation = () => {
      router.push("/manage-tournament");
    };

  return (
    <>
      <Flex
        align={'center'}
        margin={'1% 0% 2% 2%'}
        position={'relative'}
      >
        <Flex align={"center"} onClick={handleBackNavigation}>
          <FaArrowCircleLeft size={"4vh"} />
          <Text ml={"1vh"} fontSize={"3xl"}>
            Back
          </Text>
        </Flex>
        {tournament && (
          <Flex
            direction={'column'}
            margin={'auto'}
            left={'50%'}
            transform={"translateX(-20%)"}>
            <Text fontSize={'2xl'} margin={'auto'} fontWeight={'bold'}>
              {tournament.name}
            </Text>
            <Text fontSize={'xl'} margin={'auto'}>
              {formatDate(tournament.estimatedTournamentPeriod.tournamentBegin)} - {formatDate(tournament.estimatedTournamentPeriod.tournamentEnd)}
            </Text>
          </Flex>
        )}
      </Flex>
      <Box
        m={'0% 3%'}>
        <Tabs variant="enclosed" colorScheme="teal">
          <TabList>
            {tournamentData && tournamentData.map((round, index) => (
              <Tab key={index}>{round.title}</Tab>
            ))}
          </TabList>
          <TabPanels>
            {tournamentData && tournamentData.map((round, index) => (
              <TabPanel key={index} p={4}>
                {round.seeds
                  .filter(seed => seed.teams && seed.teams.length === 2 && seed.teams.every(team => !team.empty))
                  .map((seed, seedIndex) => (
                    <MatchComponent
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
    </>
  );
};

export default ManageMatchesPage;
