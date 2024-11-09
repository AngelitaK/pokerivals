"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "../../../../config/axiosInstance";
import { Suspense } from "react";
import {
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Box,
  Flex,
  Text,
  Button,
  Input,
  Divider,
  useToast,
  Heading,
} from "@chakra-ui/react";
import { FaArrowCircleLeft } from "react-icons/fa";
import TournamentForm from "@/components/tournamentForm";
// import test_data from "./test_data";
import { useDisclosure } from "@chakra-ui/react";
import PlayerProfileModal from "@/components/playerProfileModal";

const pageSize = 100;

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

const date = new Date().toISOString();

const ListComponent = ({ details, registrationEnd }) => {
  const router = useRouter();
  const toast = useToast();
  const { isOpen, onOpen, onClose } = useDisclosure();

  const handleKick = async () => {
    try {
      const response = await axios.delete(
        `http://localhost:8080/admin/tournament/${details.tournament.id}/team/player/${details.playerUsername}`
      );
      if (response.status !== 200) {
        throw new Error("Failed to kick player");
      }
    } catch (error) {
      console.error("Error kicking Player:", error);
      toast({
        title: "Error",
        description: "Failed to kick player.",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    }
  };

  return (
    <Flex
      p={4}
      m={3}
      borderRadius="md"
      backgroundColor="white"
      boxShadow="md"
      alignItems="center"
      justifyContent="space-between"
      mx="auto"
    >
      <Box>
        <Text fontSize="xl" fontWeight="semibold" color="gray.700">
          {details.playerUsername}
        </Text>
      </Box>
      <Flex>
        <Button
          mr={2}
          backgroundColor="deepskyblue"
          color="white"
          _hover={{ backgroundColor: "dodgerblue" }}
          onClick={onOpen}
        >
          View Profile
        </Button>
        <Button
          backgroundColor="tomato"
          color="white"
          _hover={{ backgroundColor: "red.600" }}
          disabled={date < registrationEnd}
          onClick={handleKick}
        >
          Kick
        </Button>
      </Flex>
      <PlayerProfileModal
        isOpen={isOpen}
        onClose={onClose}
        playerData={details}
      />
    </Flex>
  );
};

const ManageTeamPage = () => {
  const router = useRouter();
  const [page, setPage] = useState(0);
  const [tournament, setTournament] = useState(null);
  const [teams, setTeams] = useState([]);
  const [count, setCount] = useState(0);
  const toast = useToast();
  const [inviteUsername, setInviteUsername] = useState("");

  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  const handleInputChange = (e) => {
    setInviteUsername(e.target.value);
  };

  const handleBackNavigation = () => {
    router.back();
  };

  const handleStartMatch = async () => {
    try {
      const response = await axios.post(
        `http://localhost:8080/tournament/match/${id}/start`
      );
      if (response.status !== 200) {
        throw new Error("Failed to start match");
      }
      router.push(`/manage-tournament/manage-match?id=${id}`);
    } catch (error) {
      console.error("Error starting match:", error);
      toast({
        title: "Error",
        description: "Failed to start match.",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    }
  };

  const sendInvite = async () => {
    try {
      const response = await axios.post(
        `http://localhost:8080/admin/tournament/closed/${id}/invitation`,
        {
          usernames: inviteUsername.split(","),
        }
      );
      if (response.status !== 200) {
        throw new Error("Failed to invite players");
      }
    } catch (error) {
      console.error("Error inviting players:", error);
      toast({
        title: "Error",
        description: "Failed to invite players.",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    }
  };

  useEffect(() => {
    const tournaments = JSON.parse(sessionStorage.getItem("tournaments"));
    for (var t of tournaments) {
      if (t.id == id) {
        setTournament(t);
      }
    }
    const fetchTeams = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/admin/tournament/${id}/team?page=${page}&limit=${pageSize}`
        );
        // var test_response = test_data;
        if (response.status !== 200) {
          throw new Error("Failed to fetch teams");
        }
        const data = response.data;
        setTeams(data.teams);
        setCount(data.count);
      } catch (error) {
        console.error("Error fetching Teams:", error);
        toast({
          title: "Error",
          description: "Failed to load teams.",
          status: "error",
          duration: 3000,
          isClosable: true,
        });
      }
    };
    fetchTeams();
  }, [page]);

  const totalPages = Math.ceil(count / pageSize);

  return (
      <Box p="1%" backgroundColor="gray.100" minH="100vh">
        <Box>
          <Flex align="center" justify="space-between" margin="1% 0% 2% 2%">
            <Flex
              align="center"
              onClick={handleBackNavigation}
              cursor="pointer"
              flex="1"
            >
              <FaArrowCircleLeft size="4vh" />
              <Text ml="1vh" fontSize="3xl">
                Back
              </Text>
            </Flex>
            {tournament && (
              <Flex direction={"column"} margin={"auto"}>
                <Heading
                  as="h2"
                  size="lg"
                  textAlign="center"
                  fontWeight="bold"
                  color="gray.800"
                  flex="1"
                >
                  {tournament.name}
                </Heading>
                <Text fontSize={"xl"} margin={"auto"}>
                  {formatDate(
                    tournament.estimatedTournamentPeriod.tournamentBegin
                  )}{" "}
                  -{" "}
                  {formatDate(
                    tournament.estimatedTournamentPeriod.tournamentEnd
                  )}
                </Text>
              </Flex>
            )}
            <Box flex="1" />
          </Flex>
        </Box>

        <Tabs
          isFitted
          variant="enclosed"
          backgroundColor="white"
          borderRadius="lg"
          shadow="md"
          p="6"
        >
          <TabList mb="1em">
            <Tab fontWeight="bold">Edit Tournament Details</Tab>
            <Tab fontWeight="bold">Start Tournament</Tab>
          </TabList>

          <TabPanels>
            <TabPanel>
              <TournamentForm tournament={tournament} isEdited={true} />
            </TabPanel>
            <TabPanel>
              <Box>
                <Flex mt="4">
                  <Input
                    placeholder="Enter usernames"
                    value={inviteUsername}
                    onChange={handleInputChange}
                  />
                  <Button ml="2" colorScheme="blue" onClick={sendInvite}>
                    Invite
                  </Button>
                </Flex>
                <Divider mb="4" />
                <Box height="60vh" overflowY="auto">
                  {teams.map((team) => (
                    <ListComponent
                      key={team.playerUsername}
                      details={team}
                      registrationEnd={
                        tournament?.registrationPeriod.registrationEnd
                      }
                    />
                  ))}
                </Box>
                <Button
                  mt="4"
                  colorScheme="green"
                  disabled={
                    date < tournament?.registrationPeriod.registrationEnd
                  }
                  onClick={handleStartMatch}
                >
                  Start Tournament
                </Button>
              </Box>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </Box>
  );
};

export default ManageTeamPage;