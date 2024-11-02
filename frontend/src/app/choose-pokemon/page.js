"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "../../../config/axiosInstance";
import useAuth from "../../../config/useAuth";
import LoadingOverlay from "../../components/loadingOverlay";
import Link from "next/link";
import PokemonCard from "@/components/pokemonCard";
import {
  Box,
  Flex,
  Heading,
  Text,
  Image,
  Button,
  IconButton,
} from "@chakra-ui/react";
import { AddIcon, ArrowBackIcon } from "@chakra-ui/icons";

// function PokemonCard({ name, type, stats, bgColor }) {
//   return (
//     <Box bg={bgColor} borderRadius="lg" p={4} w="150px" textAlign="center">
//       <Text fontWeight="bold" color="white">
//         {name}
//       </Text>
//       <Text color="white" fontSize="sm">
//         {type}
//       </Text>
//       <Box mt={2}>
//         {Object.entries(stats).map(([key, value]) => (
//           <Text color="white" key={key}>
//             {key.toUpperCase()}: {value}
//           </Text>
//         ))}
//       </Box>
//     </Box>
//   );
// }

const ChoosePokemon = () => {
  const router = useRouter();

  const { isAuthenticated, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, loading);

  const team = [
    {
      name: "Pikachu",
      type: "Electric",
      stats: { HP: 450, ATK: 123, SPD: 123, DEF: 50, S_ATK: 117, S_DEF: 15 },
      bgColor: "orange.400",
    },
    {
      name: "Pichu",
      type: "Electric",
      stats: { HP: 450, ATK: 123, SPD: 123, DEF: 50, S_ATK: 117, S_DEF: 15 },
      bgColor: "orange.400",
    },
    {
      name: "<Choose>",
      type: "",
      stats: { HP: "?", ATK: "?", SPD: "?", DEF: "?", S_ATK: "?", S_DEF: "?" },
      bgColor: "gray.200",
    },
    {
      name: "Clefairy",
      type: "Fairy",
      stats: { HP: 450, ATK: 123, SPD: 123, DEF: 50, S_ATK: 117, S_DEF: 15 },
      bgColor: "pink.400",
    },
    {
      name: "Jigglypuff",
      type: "Normal/Fairy",
      stats: { HP: 450, ATK: 123, SPD: 123, DEF: 50, S_ATK: 117, S_DEF: 15 },
      bgColor: "purple.200",
    },
    {
      name: "Gyarados",
      type: "Flying/Water",
      stats: { HP: 450, ATK: 123, SPD: 123, DEF: 50, S_ATK: 117, S_DEF: 15 },
      bgColor: "blue.400",
    },
  ];

  if (loading) return <LoadingOverlay />;
  if (!isAuthenticated) return null;

  return (
    <Flex
      direction="column"
      align="center"
      bgImage="/bgPokemon.png"
      bgSize="cover"
      bgPosition="center"
      p={8}
      minH="100vh"
    >
      {/* Top Header */}
      <Flex w="100%" justify="center" align="center" mb={10}>
        <Button onClick={() => router.back()} colorScheme="blue" position="absolute" left="10px">
          Back
        </Button>
        <Heading size="xl">My Pokémon Team</Heading>
      </Flex>

      {/* Pokemon Team */}
      <Flex gap={8} my={10} pt={10} wrap="wrap" justify="center">
        {team.map((pokemon, index) => (
          <PokemonCard key={index} {...pokemon} />
        ))}
      </Flex>

      <Button mt={8} colorScheme="blue" size="lg" borderRadius={"lg"} minW={"500px"} minH={"60px"}>
        <Text fontSize={"2xl"}>Ready for Battle!</Text>
      </Button>
    </Flex>
  );
};

export default ChoosePokemon;