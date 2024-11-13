"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "../../../config/axiosInstance";
import checkLogged from "../../../config/checkLogged";
import LoadingOverlay from "../../components/loadingOverlay";
import { GoogleLogin } from "@react-oauth/google";
import {
  Button,
  Flex,
  Text,
  Heading,
  Stack,
  Image,
  useToast,
} from "@chakra-ui/react";

export default function Activate() {
  const router = useRouter();
  const toast = useToast();
  const searchParams = useSearchParams(); // This will give access to query parameters
  const [idToken, setIdToken] = useState("");
  const [userData, setUserData] = useState({
    username: "",
    email: "",
    time: "",
    mac: "",
  });

  // check if user is logged in
  const { isAuthenticated, loading } = checkLogged();
  console.log(isAuthenticated, loading);

  useEffect(() => {
    // Retrieve the query params using searchParams.get()
    const username = searchParams.get("username");
    const email = searchParams.get("email");
    const time = searchParams.get("time");
    const mac = searchParams.get("mac");

    if (username && email && time && mac) {
      setUserData({
        username: decodeURIComponent(username),
        email: decodeURIComponent(email),
        time,
        mac,
      });
      console.log("User data set:", { username, email, time, mac });
    } else {
      console.log("Query parameters missing");
    }
  }, [searchParams]);

  // activation using Google
  const handleGoogleActivation = async (credentialResponse) => {
    const idToken = credentialResponse.credential;
    setIdToken(idToken); // Save the credentials for registration later

    // Post the data to the backend
    try {
      const response = await axios.post("/admin/link", {
        username: userData.username,
        email: userData.email,
        time: userData.time, // Unix timestamp (long)
        mac: userData.mac,
        credentials: idToken, // Google ID token
      });
      console.log("Login successful:", response.data);

      localStorage.setItem("username", userData.username);
      localStorage.setItem("role", "ADMIN");
      document.cookie =
        "g_state=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"; // Remove the g_state cookie

      // Show success message
      toast({
        title: "Activation Successful",
        description: `Account successfully linked! Welcome ${userData.username}!`,
        status: "success",
        duration: 9000,
        isClosable: true,
      });

      router.push("/admin-home");
    } catch (error) {
      console.error("Activation failed", error);

      // Show error message
      toast({
        title: "Activation Failed",
        description:
          "There was a problem linking your account. Please try again.",
        status: "error",
        duration: 9000,
        isClosable: true,
      });
    }
  };

  //   router to login page
  const handleBackToLogin = () => {
    router.push("/login");
  };

  if (loading) return <LoadingOverlay />;
  if (isAuthenticated) return null;

  return (
      <Stack
        maxH={"100vh"}
        direction={{ base: "column", md: "row" }}
        bg="white"
      >
        <Flex flex={1} w={"full"} bg="white">
          <Image
            alt={"Register Image"}
            objectFit={"cover"}
            width={"100%"}
            minH={"89.5vh"}
            src={"/PokeRegistration.png"}
          />
        </Flex>

        {/* activation page */}
        <Flex p={3} flex={1} align={"center"} justify={"center"} bg="white">
          <Stack spacing={5} w={"full"} maxW={"md"} textAlign={"justify"}>
            <Heading fontSize={"3xl"} color="black">
              Welcome to PokeRivals!
            </Heading>

            <Text fontSize={"lg"} color={"gray.600"} textAlign={"justify"}>
              One more step until you can login! Please login with your desired
              google account to link with the new account. Note that once your
              account is linked, it cannot be used to make a player account
            </Text>

            <Stack spacing={5} mt={3}>
              <GoogleLogin
                onSuccess={handleGoogleActivation}
                onError={() => {
                  console.log("Login Failed");
                }}
              />

              <Button
                colorScheme={"gray"}
                variant={"solid"}
                onClick={handleBackToLogin}
              >
                Back to Login
              </Button>
            </Stack>
          </Stack>
        </Flex>
      </Stack>
  );
}
