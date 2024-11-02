'use client'

import { useState } from 'react';
import { useRouter } from 'next/navigation';

import {
  Center,
  Flex,
  Box,
  Stack,
  Button,
  Heading,
  Text,
  Grid,
  GridItem,
  Image
} from "@chakra-ui/react";

const Tournament = () => {
  const router = useRouter()
  const [loading, setLoading] = useState(false)

    // Sample tournament data placeholder
    const tournaments = [
      {
        name: "Royale Battle",
        pokemon_used: [],
        rank: 6,
        ratings: "Waiting Result",
      },
    ];

  return (
    <Stack
      minH={"100vh"}
      bgImage="/TournamentBG.jpg"
      bgSize="cover"
      bgPosition="center"
    >
      <Center pt={5}>
        <Stack align="center" spacing={4}>
          <Box>
            <Image
              src="/trophy.png"
              alt="Trophy"
              style={{ width: "150px", height: "auto" }}
            />
          </Box>

          {/* Button to select another tournament */}
          <Button
            colorScheme="blue"
            variant="solid"
            width="600px"
            height="70px"
            borderRadius="3xl"
            onClick={() => router.push('/find-tournament')}//to change to route to the correct page
          >
            <Text fontSize="2xl">Find A Tournament!</Text>
          </Button>
        </Stack>
      </Center>

      {/* Match results for pokemon sets used */}
      <Flex justifyContent="center">
        <Box maxWidth="1400px" width="100%" mt={5} color={"white"}>
          <Heading
            textAlign="center"
            textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
            mb={4}
          >
            Recent Match Results
          </Heading>

          <Flex
            bg="rgba(255, 255, 255, 0.8)"
            p={5}
            borderRadius="lg"
            alignItems="center"
            justifyContent="center"
            boxShadow="md"
            mb={6}
            gap={2}
            direction="column"
          >

            {/* grid for headers */}
            <Grid templateColumns="repeat(4, 1fr)" gap={6} mx={5}>
              <GridItem w="100%" h="100%" pb={5} pt={5}>
                <Text
                  color="black"
                  fontWeight="bold"
                  fontSize="xl"
                  textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
                  mb={4}
                >
                  Tournament Name
                </Text>
              </GridItem>

              <GridItem w="100%" h="100%" pb={5} pt={5}>
                <Text
                  color="black"
                  fontWeight="bold"
                  fontSize="xl"
                  paddingLeft="20px"
                  textAlign="center"
                  textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
                  mb={4}
                >
                  Pokemon Used
                </Text>
              </GridItem>

              <GridItem w="100%" h="100%" pb={5} pt={5}>
                <Text
                  color="black"
                  fontWeight="bold"
                  fontSize="xl"
                  paddingLeft="175px"
                  textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
                  mb={4}
                >
                  Rank
                </Text>
              </GridItem>

              <GridItem w="100%" h="100%" pb={5} pt={5}>
                <Text
                  color="black"
                  fontWeight="bold"
                  fontSize="xl"
                  paddingLeft="145px"
                  textShadow="2px 2px 4px rgba(0, 0, 0, 0.4)"
                  mb={4}
                >
                  Total Rating
                </Text>
              </GridItem>
            </Grid>

            {/* <PokemonSet />
            <PokemonSet />
            <PokemonSet /> */}

          </Flex>
        </Box>
      </Flex>
    </Stack>
  );
};

export default Tournament;