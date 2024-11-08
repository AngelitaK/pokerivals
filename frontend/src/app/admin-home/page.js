"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "../../../config/axiosInstance"; 
import useAuth from "../../../config/useAuth"; 
import LoadingOverlay from "../../components/loadingOverlay";
import Link from "next/link";
import Head from "next/head";
import { Flex, Stack, Button, Text } from "@chakra-ui/react";

export default function AdminHome() {
  const router = useRouter();
  // Check authentication
  const { isAuthenticated, user, loading } = useAuth("ADMIN");
  console.log(isAuthenticated, user, loading);

  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;

  return (
    <Flex minH={"100vh"} align={"center"} justify={"center"} bg="white">
      {/* page title */}
      <Head>
        <title>Home</title>
      </Head>

      <Stack spacing={8} align={"center"}>
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

        {/* manage betting */}
        <Button
          colorScheme="blue"
          variant="solid"
          width="500px"
          height="65px"
          mb={5}
          onClick={() => router.push("/manage-betting")}
        >
          <Text fontSize="2xl" color="white" fontWeight="bold">
            Manage Betting
          </Text>
        </Button>
      </Stack>
    </Flex>
  );
}