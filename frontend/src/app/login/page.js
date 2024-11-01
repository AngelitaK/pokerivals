"use client";

import { useState, useContext, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "../../../config/axiosInstance";
import Head from "next/head";
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

  //registration input
  const [userName, setUserName] = useState(""); // To store the username input
  const [description, setDescription] = useState(""); // To store the description input

  // To control the modal visibility for registration
  const [isNewUser, setIsNewUser] = useState(false);
  const { isOpen, onOpen, onClose } = useDisclosure(); // Modal control

  // Store the Google credentials
  const [idToken, setIdToken] = useState("");

  // for local storage
  const [user, setUser] = useState({
    username: null,
    role: null,
    isAuthenticated: false,
  });

  // Google login
  const handleGoogleLogin = async (credentialResponse) => {
    console.log(credentialResponse);
    const idToken = credentialResponse.credential;
    setIdToken(idToken); // Save the credentials for registration later

    try {
      const response = await axios.post("/auth/login", {
        credentials: idToken,
      });

      const data = response.data;
      console.log("Login successful:", data);

      localStorage.setItem("username", data.username);
      localStorage.setItem("role", data.role);
      document.cookie =
        "g_state=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

    //   setUser.username = data.username;
    //   setUser.role = data.role;
    //   setUser.isAuthenticated = true;

      // Show success toast
      toast({
        title: "Login Success",
        description: `Welcome ${data.username}!`,
        status: "success",
        duration: 2000,
        isClosable: true,
      });

      // Redirect based on role
      if (data.role === "ADMIN") {
        router.push("/admin-home");
      } else if (data.role === "PLAYER") {
        router.push("/find-tournament");
      }
    } catch (error) {
      console.error("Login failed", error);

      // Open the modal for new user registration if error is 404
      setIsNewUser(true);
      onOpen();
    }
  };

  // Registration for new users
  const handleRegistration = async () => {
    try {
      const response = await axios.post("/player",
        {
          credentials: idToken,
          player: { username: userName, description: description },
        }
      );
      console.log(response.data);

    //   localStorage.setItem("username", userName);
    //   localStorage.setItem("role", "PLAYER");
    //   document.cookie =
    //     "g_state=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    //   setUser.username = userName;
    //   setUser.role = "PLAYER";
    //   setUser.isAuthenticated = true;

      await handleGoogleLogin({ credential: idToken });
      router.push("/choose-clan");
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
      <Stack
        maxH={"100vh"}
        direction={{ base: "column", md: "row" }}
        bg="white"
      >
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
