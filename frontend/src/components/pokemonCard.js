'use client'

import { Box, VStack, Text, Image, HStack, Tag } from '@chakra-ui/react';
import { AddIcon } from '@chakra-ui/icons';


function StatRow({ label, value }) {
  return (
    <HStack justify="space-between">
      <Text fontSize="sm" fontWeight="medium">
        {label}
      </Text>
      <Text fontSize="sm">{value}</Text>
    </HStack>
  )
}

export default function pokemonCard({ pokemon = null }) {
  const emptyStats = {
    name: 'Choose',
    hp: '?',
    atk: '?',
    spd: '?',
    def: '?',
    sAtk: '?',
    sDef: '?',
    types: []
  }

  const stats = pokemon || emptyStats

  return (
    <Box position="relative" minW="250px">
      <Box
        position="absolute"
        top="-60px"
        left="50%"
        transform="translateX(-50%)"
        zIndex={2}
        // mb={10}  
      >
        {/* pokemon data from BE */}
        {pokemon ? (
        // add pokemon sprite here
          <Image
            src="/placeholder.svg?height=80&width=80"
            alt={pokemon?.name || 'Pokemon'}
            width="80px"
            height="80px"
          />
        ) : (
          <Box
            width="40px"
            height="40px"
            borderRadius="full"
            bg="gray.200"
            display="flex"
            alignItems="center"
            justifyContent="center"
          >
            <AddIcon />
          </Box>
        )}
      </Box>

      {/* stats */}
      <Box
        bg={pokemon ? 'yellow.400' : 'white'}
        borderRadius="lg"
        p={4}
        pt={12}
        boxShadow="md"
      >
        <VStack spacing={5} align="stretch">
          <Text fontSize="xl" fontWeight="bold" textAlign="center">
            {stats.name}
          </Text>
          
          {pokemon && (
            <HStack justify="center" spacing={2} mb={2}>
              {stats.types.map((type, index) => (
                <Tag key={index} colorScheme="gray" size="sm">
                  {type}
                </Tag>
              ))}
            </HStack>
          )}

          {/* Pokemon stats */}
          <StatRow label="HP" value={stats.hp} />
          <StatRow label="ATK" value={stats.atk} />
          <StatRow label="SPD" value={stats.spd} />
          <StatRow label="DEF" value={stats.def} />
          <StatRow label="S.ATK" value={stats.sAtk} />
          <StatRow label="S.DEF" value={stats.sDef} />
        </VStack>
      </Box>
    </Box>
  )
}
