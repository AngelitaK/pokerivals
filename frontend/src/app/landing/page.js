"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
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

  return (
    <Stack spacing={0}>
      <Stack
        minH={"65vh"}
        bgImage="/PokeRivalsBackgroundLanding1.png"
        bgSize="cover"
        bgPosition="center"
      >
        <Center pt={5}>
          <Stack align="center" spacing={4}>
            <Box>
              <Image
                src="/PokeLogo.png"
                alt="PokeRivals Logo"
                style={{ width: "500px", height: "auto" }}
              />
            </Box>

            {/* Buttons */}
            <Button
              colorScheme="blue"
              variant="solid"
              width="500px"
              height="65px"
              mb={5}
              onClick={() => router.push("/login")}
            >
              <Text fontSize="2xl" color="black" fontWeight="bold">Play Now</Text>
            </Button>

            <Button
              colorScheme="blue"
              variant="solid"
              width="500px"
              height="65px"
              mb={5}
              onClick={() => router.push("/home")}
            >
              <Text fontSize="2xl" color="black" fontWeight="bold">Watch Live Matches</Text>
            </Button>
          </Stack>
        </Center>
      </Stack>

      {/* bottom section */}
      <Stack bg="#FFC700" bgSize="cover" bgPosition="center">
        <Heading as="h1" size="2xl" textAlign="center" py={"30px"} color="black">
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
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
            eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim
            ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut
            aliquip ex ea commodo consequat. Duis aute irure dolor in
            reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
            pariatur. Excepteur sint occaecat cupidatat non proident, sunt in
            culpa qui officia deserunt mollit anim id est laborum.Lorem ipsum
            dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
            incididunt ut labore et dolore magna aliqua. Ut enim ad minim
            veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex
            ea commodo consequat. Duis aute irure dolor in reprehenderit in
            voluptate velit esse cillum dolore eu fugiat nulla pariatur.
          </Text>
        </Box>

      </Stack>
    </Stack>
  );
}