"use client";
import { useEffect, useState } from 'react';
import { useToast } from '@chakra-ui/react';
import { useRouter } from "next/navigation";
import axios from 'axios';
import LoadingOverlay from '../../components/loadingOverlay';
import { Flex, Container, VStack, Box, Heading, Text, Input, Button, List, ListItem, Badge } from '@chakra-ui/react';
import { AddIcon, EmailIcon } from '@chakra-ui/icons';

export default function AddAdmin() {
    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [isInviting, setIsInviting] = useState(false);
    const [invitedPeople, setInvitedPeople] = useState([]);
    const [loading, setLoading] = useState(true); // Loading state
    const [isUser, setIsUser] = useState(false); // User authentication state
    const toast = useToast();
    const router = useRouter();

    // Check for session ID on component mount
    useEffect(() => {
        const checkSession = async () => {
            try {
                setLoading(true); // Show loading overlay

                //  verify session with an API call
                const response = await axios.get('http://localhost:8080/me', {
                    withCredentials: true,
                    headers: {
                        "Content-Type": "application/json",
                    },
                });

                console.log("Session check response:", response.data);

                if(response.data.role === "ADMIN"){
                  setIsUser(true); // Set user state to true if authenticated
                }
                else{
                  router.push('/login'); // Redirect if not authenticated
                }
              
            } catch (error) {
                console.error("Session check failed:", error);
                router.push('/login'); // Redirect on error
            } finally {
                setLoading(false); // Hide loading overlay after check
            }
        };

        checkSession();
    }, [router]);

    // Fetch invited admins with useEffect
    useEffect(() => {
        const loadInvitedAdmins = async () => {
            try {
                const response = await axios.get("http://localhost:8080/admin/me/invitee", {
                    withCredentials: true,
                    headers: {
                        "Content-Type": "application/json",
                    },
                });

                const admins = response.data;
                console.log(admins);

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
            const response = await axios.post("http://localhost:8080/admin", {
                username: username,
                email: email,
            }, {
                withCredentials: true,
                headers: {
                    "Content-Type": "application/json",
                },
            });

            toast({
                title: "Invitation Sent",
                description: "Invitation sent successfully to the user.",
                status: "success",
                duration: 3000,
                isClosable: true,
            });

            setInvitedPeople([...invitedPeople, { id: String(Date.now()), email, username, active: false }]);
            setEmail("");
            setUsername("");
        } catch (error) {
            toast({
                title: "Error",
                description: error.response?.data?.message || "Failed to send invitation",
                status: "error",
                duration: 3000,
                isClosable: true,
            });
        } finally {
            setIsInviting(false);
        }
    };

    return (
        <Flex minH="90vh" bg="white">
            <Container maxW="container.2xl" my={10} mx={{ lg: 8, xl: "10%" }}>
                
                {/* Show loading overlay when checking session */}
                {loading && <LoadingOverlay />}

                {/* Render main content only if not loading and user is authenticated */}
                {!loading && isUser && (
                    <>
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
                                        <Input type="text" placeholder="Enter username" value={username} onChange={(e) => setUsername(e.target.value)} required mb={3} />
                                        <Input type="email" placeholder="Enter email" value={email} onChange={(e) => setEmail(e.target.value)} required mb={4} />
                                    </VStack>
                                    <Button type="submit" colorScheme="blue" isLoading={isInviting} loadingText="Inviting" leftIcon={<AddIcon />}>
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
                                                    <Button leftIcon={<EmailIcon />} size="sm" colorScheme="blue" variant="outline">
                                                        Resend link email
                                                    </Button>
                                                )}
                                            </Flex>
                                        </ListItem>
                                    ))}
                                </List>
                            </Box>
                        </VStack>
                    </>
                )}
                
            </Container>
        </Flex>
    );
}