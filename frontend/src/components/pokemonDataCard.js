"use client";

import React from "react";
import {
  Flex,
  Box,
  VStack,
  Text,
  Heading,
  HStack,
  Tag,
  Image,
} from "@chakra-ui/react";

// Component for displaying a stat row
function StatRow({ label, value }) {
  return (
    <HStack justify="space-between">
      <Text fontSize="md" fontWeight="medium">
        {label}
      </Text>
      <Text fontSize="md">{value}</Text>
    </HStack>
  );
}

function PokemonDataCard({ pokemonData }) {
  if (!pokemonData) {
    return null; // Return null if no data is passed
  }

  const { nature, moves, ability, pokemon } = pokemonData;
  const { name, id, stats } = pokemon;

  return (
    <Box width="280px" bg="yellow.400" borderRadius="lg" p={3} boxShadow="md">
      {/* Pokémon Image */}
      <Image
        src={`https://pokerivals-assets.s3.ap-southeast-1.amazonaws.com/sprites/${name}.png`}
        width={"180px"}
        height={"auto"}
        alt={name}
        mx="auto"
        mb={2}
      />

      {/* Pokémon Name and Nature */}
      <Text fontSize="xl" fontWeight="bold" textAlign="center">
        {name}
      </Text>
      <Text fontSize="md" textAlign="center" color="gray.700">
        Nature: {nature}
      </Text>

      {/* Pokémon Ability */}
      <Text fontSize="md" textAlign="center" color="gray.700">
        Ability: {ability}
      </Text>

      {/* Pokémon Moves */}
      <VStack align="start" mt={2} spacing={1}>
        <Text fontWeight="bold" fontSize="lg">
          Moves
        </Text>
        {moves.map((move, idx) => (
          <Text key={idx} fontSize="md">
            {move}
          </Text>
        ))}
      </VStack>

      {/* Pokémon Stats */}
      <VStack align="stretch" mt={4} spacing={1}>
        <StatRow label="HP" value={stats.hp} />
        <StatRow label="Attack" value={stats.attack} />
        <StatRow label="Defense" value={stats.defense} />
        <StatRow label="Speed" value={stats.speed} />
        <StatRow label="Sp. Attack" value={stats.spA} />
        <StatRow label="Sp. Defense" value={stats.spD} />
      </VStack>
    </Box>
  );
}

export default PokemonDataCard;
