"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import LoadingOverlay from "../../components/loadingOverlay";
import Link from "next/link";
import Head from "next/head";
import { Flex, Stack, Button, Text } from "@chakra-ui/react";

export default function AdminHome() {
  const [isUser, setIsUser] = useState(false); // User authentication state
  const [loading, setLoading] = useState(true); // Loading state
  const router = useRouter();

  // Check for session ID on component mount
  useEffect(() => {
    const checkSession = async () => {
      try {
        setLoading(true); // Show loading overlay

        //  verify session with an API call
        const response = await axios.get("http://localhost:8080/test", {
          withCredentials: true,
          headers: {
            "Content-Type": "application/json",
          },
        });

        console.log("Session check response:", response.data);

        if (response.data.role === "ADMIN") {
          setIsUser(true); // Set user state to true if authenticated
        } else {
          router.push("/login"); // Redirect if not authenticated
        }
      } catch (error) {
        console.error("Session check failed:", error);
        router.push("/login"); // Redirect on error
      } finally {
        setLoading(false); // Hide loading overlay after check
      }
    };

    checkSession();
  }, [router]);

  return (
    <Flex minH={"100vh"} align={"center"} justify={"center"} bg="white">
      {/* page title */}
      <Head>
        <title>Home</title>
      </Head>

      <Stack spacing={8} align={"center"}>
        {/* Show loading overlay when checking session */}
        {loading && <LoadingOverlay />}

        {/* Render main content only if not loading and user is authenticated */}
        {!loading && isUser && (
          <>
            <Button
              colorScheme="blue"
              variant="solid"
              width="500px"
              height="65px"
              mb={5}
              onClick={() => router.push("/create-tournament")}
            >
              <Text fontSize="2xl" color="white" fontWeight="bold">
                Create Tournament
              </Text>
            </Button>

            {/* manage tourn */}
            <Button
              colorScheme="blue"
              variant="solid"
              width="500px"
              height="65px"
              mb={5}
              onClick={() => router.push("/manage-tournament")}
            >
              <Text fontSize="2xl" color="white" fontWeight="bold">
                Manage Tournament
              </Text>
            </Button>

            {/* add admins */}
            <Button
              colorScheme="blue"
              variant="solid"
              width="500px"
              height="65px"
              mb={5}
              onClick={() => router.push("/add-admin")}
            >
              <Text fontSize="2xl" color="white" fontWeight="bold">
                Add Admins
              </Text>
            </Button>
          </>
        )}
      </Stack>
    </Flex>
  );
}