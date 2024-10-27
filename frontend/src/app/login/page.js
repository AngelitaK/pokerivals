"use client";

import { useState } from "react";
import { useContext } from "react";
import { UserContext } from "../Providers.js";
import { useRouter } from "next/navigation";
import axios from "axios";
import Head from 'next/head'; 
import { GoogleLogin } from "@react-oauth/google";
import {
    Box,
    Flex,
    Text,
    Heading,
    Stack,
    Image,
    useToast,
    Button,
    Input,
    Modal,
    ModalOverlay,
    ModalContent,
    ModalHeader,
    ModalBody,
    ModalFooter,
    useDisclosure,
    Textarea,
} from "@chakra-ui/react";

export default function Login() {
    const toast = useToast();
    const router = useRouter();
    const { userData, setUserData } = useContext(UserContext);

    const [isNewUser, setIsNewUser] = useState(false); // To control the modal visibility
    const [userName, setUserName] = useState(""); // To store the username input
    const [description, setDescription] = useState(""); // To store the username input
    const [idToken, setIdToken] = useState(""); // Store the Google credentials
    const { isOpen, onOpen, onClose } = useDisclosure(); // Modal control

    // Fetch User Data
    const fetchUserData = async (username) => {
        try {
            const response = await fetch(`http://localhost:8080/player/${username}`, {
                method: "GET",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch user data: ${response.statusText}`);
            }

            const data = await response.json();
            setUserData(data); // Store the fetched user data
        } catch (error) {
            console.error("Error fetching user data:", error);
        }
    };

    // Google login
    const handleGoogleLogin = async (credentialResponse) => {
        console.log(credentialResponse);
        const idToken = credentialResponse.credential;
        setIdToken(idToken); // Save the credentials for registration later

        // Send the ID token to the backend
        try {
            const response = await axios.post("http://localhost:8080/auth/login", {
                credentials: idToken,
            }, {
                withCredentials: true,
                headers: {
                    "Content-Type": "application/json",
                },
            });

            const data = response.data;
            console.log("Login successful:", data);

            // Case 1: User is already registered
            toast({
                title: "Login Success",
                description: `Welcome ${data.username}!`,
                status: "success",
                duration: 2000,
                isClosable: true,
            });

            // Remove the g_state cookie after successful login
            document.cookie = "g_state=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

            console.log(userData);
            await fetchUserData(data.username);

            // Redirect to admin dashboard
            if (data.role === "ADMIN") {
                router.push("/admin-home");
            }
            // Redirect to player dashboard
            else if (data.role === "PLAYER") {
                router.push("/choose-clan");
            }
        } catch (error) {
            console.error("Login failed", error);

            // Open the modal for new user registration
            setIsNewUser(true);
            onOpen();
        }
    };

    // Case 2: User is not registered
    const handleRegistration = async () => {
        console.log(userName, idToken);
    
        try {
            const response = await axios.post("http://localhost:8080/player", {
                player: {
                    username: userName,
                    description: description,
                },
                credentials: idToken,
            }, {
                headers: {
                    "Content-Type": "application/json",
                },
            });
    
            // Extract the message from the response data
            const { message } = response.data;
    
            toast({
                title: "Registration Successful",
                description: message, // Use the extracted message here
                status: "success",
                duration: 2000,
                isClosable: true,
            });
    
            document.cookie = "g_state=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
            router.push("/find-tournament");
        } catch (error) {
            console.error("Registration failed", error);
            toast({
                title: "Registration Failed",
                description: "Please try again.",
                status: "error",
                duration: 2000,
                isClosable: true,
            });
        }
    };
    

    return (
        <>
            <Head>
                <title>Login</title>
            </Head>
            <Stack maxH={"100vh"} direction={{ base: "column", md: "row" }} bg="white">
                <Flex flex={1} w={"full"}>
                    <Image
                        alt={"Login Image"}
                        objectFit={"cover"}
                        width={"inherit"}
                        minH={"83.5vh"}
                        src={"/PokeRegistration.png"}
                    />
                </Flex>

                <Flex p={5} flex={1} align={"center"} justify={"center"}>
                    <Stack spacing={3} w={"full"} maxW={"md"}>
                        <Box align={"center"} justify={"center"} mb={8}>
                            <Image
                                src={"/PokeLogo.png"}
                                alt={"Login Image"}
                                width={"400px"}
                                height={"auto"}
                            />
                        </Box>

                        <Heading fontSize={"4xl"} align={"center"} color="black" mb={10}>
                            Login
                        </Heading>

                        <Text fontSize={"sm"} color={"gray.600"} textAlign={"center"}>
                            Admin | Players
                        </Text>

                        {/* Google login */}
                        <Flex justify="center" align="center">
                            <GoogleLogin onSuccess={handleGoogleLogin} />

                            {/* Modal for new user registration */}
                            <Modal isOpen={isOpen} onClose={onClose}>
                                <ModalOverlay />
                                <ModalContent>
                                    <ModalHeader color="black">
                                        Looks like you do not have an account...
                                    </ModalHeader>
                                    <ModalBody color="black">
                                        <p>Do you want to register as a player?</p>
                                        <Input
                                            placeholder="Enter your username"
                                            value={userName}
                                            onChange={(e) => setUserName(e.target.value)}
                                            mt={4}
                                        />
                                        <Textarea
                                            placeholder="Enter your description"
                                            value={description}
                                            onChange={(e) => setDescription(e.target.value)}
                                            mt={4}
                                            size="sm"
                                        />
                                    </ModalBody>

                                    <ModalFooter>
                                        <Button colorScheme="blue" onClick={handleRegistration}>
                                            Register!
                                        </Button>
                                    </ModalFooter>
                                </ModalContent>
                            </Modal>
                        </Flex>
                    </Stack>
                </Flex>
            </Stack>
        </>
    );
}