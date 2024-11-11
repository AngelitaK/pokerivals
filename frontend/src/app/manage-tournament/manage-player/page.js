"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "../../../../config/axiosInstance";
import useAuth from "../../../../config/useAuth";
import LoadingOverlay from "../../../components/loadingOverlay";
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
  Container,
  VStack,
  List,
  ListItem
} from "@chakra-ui/react";
import { FaArrowCircleLeft } from "react-icons/fa";
import TournamentForm from "@/components/tournamentForm";
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

const ListComponent = ({ details, registrationEnd, onKick }) => {
  const toast = useToast();
  const { isOpen, onOpen, onClose } = useDisclosure();

  const handleKick = () => onKick(details)

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

  const [reload, setReload] = useState(false);
  const [tournament, setTournament] = useState(null);
  const [teams, setTeams] = useState([]);
  const toast = useToast();
  const [inviteUsername, setInviteUsername] = useState("");

  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  const { isAuthenticated, user, loading } = useAuth("ADMIN");

  const handleInputChange = (e) => {
    setInviteUsername(e.target.value);
  };

  const handleBackNavigation = () => {
    router.back();
  };

  const handleStartMatch = async () => {
    try {
      const response = await axios.post(
        `/tournament/match/${id}/start`
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

  const handleInvite = async () => {
    try {
      const response = await axios.post(
        `/admin/tournament/closed/${id}/invitation`,
        {
          usernames: inviteUsername.split(","),
        }
      );

      if (response.status !== 200) {
        throw new Error("Failed to invite players");
      } else {
        toast({
          title: "Success!",
          description: "Invited player successfully",
          status: "success",
          duration: 3000,
          isClosable: true,
        });
        setInviteUsername("")
      }

      setReload(!reload)

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

  const handleKick = async (detail) => {
    try {
      const response = await axios.delete(
        `/admin/tournament/${detail.tournament.id}/team/player/${detail.playerUsername}`
      );
      if (response.status !== 200) {
        throw new Error("Failed to kick player");
      }

      setReload(!reload)

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


  useEffect(() => {
    const fetchTeams = async () => {
      const page = sessionStorage.getItem("page");
      const pageSize = sessionStorage.getItem("pageSize");

      try {
        const response = await axios.get(
          `/admin/tournament/me?page=${page}&limit=${pageSize}`
        );

        if (response.status !== 200) {
          throw new Error("Failed to fetch teams");
        }
        const data = response.data;

        for (var t of data.tournaments) {
          if (t.id == id) {
            setTournament(t);
          }
        }
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

      try {
        const response = await axios.get(
          `/admin/tournament/${id}/team?page=${0}&limit=${100}`
        );
        
        if (response.status !== 200) {
          throw new Error("Failed to fetch teams");
        }
        const data = response.data;
        setTeams(data.teams);
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

  }, [reload]);

  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;

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
          {tournament?.['@type'] !== 'open' &&
            <Tab fontWeight="bold">Players Invited</Tab>
          }
          <Tab fontWeight="bold">Start Tournament</Tab>
        </TabList>

        <TabPanels>
          <TabPanel>
            <TournamentForm tournament={tournament} isEdited={true} />
          </TabPanel>
          {tournament?.['@type'] !== 'open' &&
            <TabPanel>
              <Flex minH="90vh" bg="white">
                <Container maxW="container.2xl" my={10} mx={{ lg: 8, xl: "10%" }}>
                  <VStack spacing={8} align="stretch" color="black">
                    <Box bg="white" shadow="md" borderRadius="lg" p={6}>
                      <Heading size="lg" mb={4}>
                        Invite Players
                      </Heading>
                      <Text mb={5}>Please enter the username below. <i>When inviting multiple players, separate the usernames with a comma without space (e.g. John Doe,Jane Doe)</i></Text>

                      {/* form to invite admin */}
                      <Flex mt="4">
                        <Input
                          placeholder="Enter username(s) here"
                          value={inviteUsername}
                          onChange={handleInputChange}
                        />
                        <Button ml="2" colorScheme="blue" onClick={handleInvite}>
                          Invite
                        </Button>
                      </Flex>
                    </Box>

                    {/* List of Invited Admins */}
                    <Box bg="white" shadow="md" borderRadius="lg" p={6}>
                      <Heading size="lg" mb={4}>
                        People I Invited
                      </Heading>

                      <List spacing={5}>
                        {tournament?.invited_players.map((person) => (
                          <ListItem mb={8}>
                            <Text fontWeight="bold">{person}</Text>
                          </ListItem>
                        ))}
                      </List>

                    </Box>
                  </VStack>
                </Container>
              </Flex>
            </TabPanel>}
          <TabPanel>
            <Box>
              <Heading size="md">Players Registered</Heading>
              <Divider mb="4" />
              <Box height="60vh" overflowY="auto">
                {teams.map((team) => (
                  <ListComponent
                    key={team.playerUsername}
                    details={team}
                    registrationEnd={
                      tournament?.registrationPeriod.registrationEnd
                    }
                    onKick={handleKick}
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