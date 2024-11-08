"use client";
import { useEffect, useState } from "react";
import { useToast } from "@chakra-ui/react";
import { useRouter } from "next/navigation";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import LoadingOverlay from "../../components/loadingOverlay";
import {
  Flex,
  Container,
  VStack,
  Box,
  Heading,
  Text,
  Input,
  Button,
  List,
  ListItem,
  Badge,
} from "@chakra-ui/react";
import { AddIcon, EmailIcon } from "@chakra-ui/icons";

export default function AddAdmin() {
  const { isAuthenticated, user, loading } = useAuth("ADMIN");
  console.log(isAuthenticated, user, loading);

  const toast = useToast();
  const router = useRouter();
  const [invitedPeople, setInvitedPeople] = useState([]);
  const [isInviting, setIsInviting] = useState(false);

  //form input for inviting admin
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");

    //test
  const fetchUserData = async () => {
    try {
        const response = await axios.get('/me');
        const data = response.data; 
        console.log("User data fetched:", data);
    } catch (error) {
        console.error("Error fetching user data:", error);
    }
};

  // Fetch invited admins with useEffect
  useEffect(() => {
    fetchUserData();

    const loadInvitedAdmins = async () => {
      try {
        const response = await axios.get("/admin/me/invitee", {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        });
        const admins = response.data;
        console.log(admins);

        if (admins.length !== 0) {
          setInvitedPeople(admins);
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
      const response = await axios.post("/admin",
        { username, email },
        {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        }
      );
      console.log(response.data);

      toast({
        title: "Invitation Sent",
        description: "Invitation sent successfully to the user.",
        status: "success",
        duration: 3000,
        isClosable: true,
      });

      setInvitedPeople((prevPeople) => [
        ...prevPeople,
        { id: String(Date.now()), email, username, active: false },
      ]);

      // Clear input fields
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

  // Render conditional UI after all hooks have been called
  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;

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
            <Text mb={5}>Please enter your Chosen here.</Text>

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

                      {/* will change once admin completes registers */}
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