"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import {
  Box,
  Button,
  Container,
  Flex,
  Heading,
  Input,
  Text,
  VStack,
  useToast,
  List,
  ListItem,
  Badge,
} from "@chakra-ui/react";
import { AddIcon, EmailIcon } from "@chakra-ui/icons";

export default function AddAdmin() {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [isInviting, setIsInviting] = useState(false);
  const [invitedPeople, setInvitedPeople] = useState([]);
  const toast = useToast();
  const router = useRouter();

  // Combined fetch invited admins with useEffect
  useEffect(() => {
    const loadInvitedAdmins = async () => {
      try {
        const response = await axios.get(
          "http://localhost:8080/admin/me/invitee",
          {
            withCredentials: true,
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        const admins = response.data;
        console.log(admins);

        // If no admins are invited yet, handle the empty response
        if (admins.length !== 0) {
          setInvitedPeople(admins); // Set the invited admins
        }
      } catch (error) {
        console.error("Error fetching invited admins:", error);
        setInvitedPeople([]); // Clear the list
      }
    };

    loadInvitedAdmins();
  }, []);

  // Handle inviting a new admin
  const handleInvite = async (e) => {
    e.preventDefault();
    setIsInviting(true);

    try {
      const response = await axios.post(
        "http://localhost:8080/admin",
        {
          username: username,
          email: email,
        },
        {
          withCredentials: true,
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      toast({
        title: "Invitation Sent",
        description: "Invitation sent successfully to the user.",
        status: "success",
        duration: 3000,
        isClosable: true,
      });

      // Add the newly invited admin to the list
      setInvitedPeople([
        ...invitedPeople,
        { id: String(Date.now()), email, username, active: false },
      ]);

      // Clear the form fields after inviting
      setEmail("");
      setUsername("");
    } catch (error) {
      toast({
        title: "Error",
        description:
          error.response?.data?.message || "Failed to send invitation",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    } finally {
      setIsInviting(false);
    }
  };

  const resendInviteEmail = async (person) => {
    try {
      console.log(person.username, person.email);
      const currentTimeInSeconds = Math.floor(Date.now() / 1000);

      const response = await axios.post(
        "http://localhost:8080/admin/link",
        {
          username: person.username,
          email: person.email,
          time: currentTimeInSeconds, // current timestamp
        },
        {
          withCredentials: true,
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      console.log("Resend invite response:", response.data);
      alert(`Invitation resent to ${person.username}`);
    } catch (error) {
      console.error(
        "Failed to resend invite:",
        error.response?.data || error.message
      );
      alert(
        `Failed to resend invite: ${
          error.response?.data?.message || error.message
        }`
      );
    }
  };

  return (
    <Flex minH="90vh" bg="white">
      <Container maxW="container.2xl" my={10} mx={{ lg: 8, xl: "10%" }}>
        <Button onClick={() => router.back()} colorScheme="blue" mb={10}>
          Back
        </Button>

        <VStack spacing={8} align="stretch" color="black">
          <Box bg="white" shadow="md" borderRadius="lg" p={6}>
            <Heading size="lg" mb={4}>
              Invite Admin
            </Heading>
            <Text mb={5}>Strictly to Google domain emails only.</Text>

            {/* form to invite admin */}
            <form onSubmit={handleInvite}>
              <VStack my={5}>
                <Input
                  type="text"
                  placeholder="Enter username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  mb={3}
                />
                <Input
                  type="email"
                  placeholder="Enter email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  mb={4}
                />
              </VStack>
              <Button
                type="submit"
                colorScheme="blue"
                isLoading={isInviting}
                loadingText="Inviting"
                leftIcon={<AddIcon />}
              >
                Invite Admin
              </Button>
            </form>
          </Box>

          {/* List of Invited Admins */}
          <Box bg="white" shadow="md" borderRadius="lg" p={6}>
            <Heading size="lg" mb={4}>
              People I Invited
            </Heading>

            <List spacing={5}>
              {invitedPeople.map((person) => (
                <ListItem key={person.id} mb={8}>
                  <Flex justify="space-between" align="center">
                    <Box>
                      <Text fontWeight="bold">{person.username}</Text>
                      <Text>{person.description}</Text>
                      <Badge colorScheme={person.active ? "green" : "yellow"}>
                        {person.active ? "Active" : "Waiting for confirmation"}
                      </Badge>
                    </Box>

                    {!person.active && (
                      <Button
                        leftIcon={<EmailIcon />}
                        size="sm"
                        colorScheme="blue"
                        variant="outline"
                        onClick={() => resendInviteEmail(person)}
                      >
                        Resend link email
                      </Button>
                    )}
                  </Flex>
                </ListItem>
              ))}
            </List>
          </Box>
        </VStack>
      </Container>
    </Flex>
  );
}
