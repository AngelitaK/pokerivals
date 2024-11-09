"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import Head from "next/head";
import {
  Center,
  Flex,
  Box,
  Stack,
  Button,
  Text,
  Heading,
  SimpleGrid,
  Image,
} from "@chakra-ui/react";

export default function Landing() {
  const router = useRouter();

    const checkUserRole = async () => {
      const storedRole = localStorage.getItem("role");
      
      if (storedRole === "ADMIN") {
        router.push("/admin-home");
      } else if (storedRole === "PLAYER") {
        router.push("/find-tournament");
      }
      else{
        router.push("/login");
      }
    };

  return (
    <>
      <Head>
        <title>PokeRivals - Pokemon Tournament Platform</title>
      </Head>

      <Stack spacing={0}>
        <Stack
          minH={"65vh"}
          bgImage="/PokeRivalsBackgroundLanding1.png"
          bgSize="cover"
          bgPosition="center"
        >
          <Stack align="center" spacing={4}>
            <Box>
              <Image
                src="/PokeLogo.png"
                alt="PokeRivals Logo"
                style={{ width: "500px", height: "auto" }}
              />
            </Box>
            <Button
              colorScheme="blue"
              variant="solid"
              width="500px"
              height="65px"
              mb={5}
              onClick={checkUserRole}
            >
              <Text fontSize="2xl" color="black" fontWeight="bold">
                Play Now
              </Text>
            </Button>
          </Stack>
        </Stack>

        {/* bottom section */}
        <Stack bg="#FFC700" bgSize="cover" bgPosition="center">
          <Heading
            as="h1"
            size="2xl"
            textAlign="center"
            py={"30px"}
            color="black"
          >
            JOIN US!
          </Heading>

          <Center>
            <SimpleGrid columns={3} spacing="150px">
              {/* first section */}
              <Box>
                <Flex direction="column" align="center">
                  <Image
                    src="/People.png"
                    alt="People Icon"
                    style={{ width: "120px", height: "auto" }}
                  />
                  <Text
                    fontSize="2xl"
                    textAlign="center"
                    fontWeight="bold"
                    color="black"
                  >
                    5M+ Players
                  </Text>
                </Flex>
              </Box>

              {/* second section */}
              <Box>
                <Flex direction="column" align="center">
                  <Image
                    src="/Shield.png"
                    alt="Shield Icon"
                    style={{ width: "120px", height: "auto" }}
                  />
                  <Text
                    fontSize="2xl"
                    textAlign="center"
                    fontWeight="bold"
                    color="black"
                  >
                    300K+ Tournaments
                  </Text>
                </Flex>
              </Box>

              {/* third section */}
              <Box>
                <Flex direction="column" align="center">
                  <Image
                    src="/MoneyBag.png"
                    alt="Money Bag Icon"
                    style={{ width: "120px", height: "auto" }}
                  />
                  <Text
                    fontSize="2xl"
                    textAlign="center"
                    fontWeight="bold"
                    color="black"
                  >
                    Win Cash
                  </Text>
                </Flex>
              </Box>
            </SimpleGrid>
          </Center>

          {/* paragraphs */}
          <Box textAlign="center" color="black" px="100px" py="50px">
            <Text>
              PokeRivals is an online platform for Pokémon fans to connect,
              compete, and engage in tournaments. It’s designed to bring
              together players who share a passion for strategic battles, with
              tournaments where they can test their skills against others in the
              community. Alongside competitive gameplay, PokeRivals also offers
              a unique betting system, allowing spectators and players to bet on
              outcomes, adding an extra layer of excitement. With its
              community-focused design, PokeRivals creates an immersive
              experience for Pokémon enthusiasts to enjoy the thrill of battles
              and friendly wagers.
            </Text>
          </Box>
        </Stack>
      </Stack>
    </>
  );
}