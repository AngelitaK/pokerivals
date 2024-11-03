"use client";

import React, { useState, useEffect } from "react";
import axios from "../../config/axiosInstance";
import SearchBar from "@/components/searchBar";
import {
  Box,
  VStack,
  Text,
  Heading,
  HStack,
  Tag,
  Spinner,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalBody,
  Button,
  Wrap,
} from "@chakra-ui/react";
import { AddIcon, CheckIcon } from "@chakra-ui/icons";

// for pokemon stats
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

function PokemonCard({ addToTeam }) {
  const [isOpen, setIsOpen] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [pokemon, setPokemon] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchResults, setSearchResults] = useState([]);
  const [selectedMoves, setSelectedMoves] = useState([]);
  const [selectedNature, setSelectedNature] = useState(null);
  const [natures, setNatures] = useState([]);
  const [selectedAbility, setSelectedAbility] = useState(null);

  // Fetch available natures
  useEffect(() => {
    const fetchNatures = async () => {
      try {
        const response = await axios.get("/pokemon/nature");
        setNatures(response.data);
      } catch (error) {
        console.error("Error fetching natures:", error);
      }
    };
    fetchNatures();
  }, []);

  // search for pokemon
  const handleSearch = async (query) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      const response = await axios.get("/pokemon/search", {
        params: { query: query, page: 0, limit: 10 },
      });
      setSearchResults(response.data.pokemons);
    } catch (error) {
      console.error("Error searching Pokémon:", error);
    }
  };

  // fetch specific pokemon using id
  const fetchPokemon = async (id) => {
    setLoading(true);

    try {
      const response = await axios.get(`/pokemon/${id}`);
      setPokemon(response.data);
      setCurrentStep(2); // Move to move selection step
    } catch (error) {
      console.error("Error fetching Pokemon:", error);
    } finally {
      setLoading(false);
    }
  };

  // handle move selection choose up to 4 moves
  const handleMoveSelection = (move) => {
    if (selectedMoves.includes(move)) {
      setSelectedMoves(selectedMoves.filter((m) => m !== move));
    } else if (selectedMoves.length < 4) {
      setSelectedMoves([...selectedMoves, move]);
    }
  };

  //add to team
  const confirmAddToTeam = () => {
    if (selectedMoves.length > 0 && selectedNature && selectedAbility) {
      const pokemonWithDetails = {
        ...pokemon,
        moves: selectedMoves,
        nature: selectedNature,
        ability: selectedAbility,
      };
      addToTeam(pokemonWithDetails);
      // Reset after adding to the team
      setIsOpen(false);
      setCurrentStep(1);
    } else {
      alert("Please complete all selections.");
    }
  };

  const stats = pokemon
    ? pokemon.stats
    : {
        hp: "?",
        attack: "?",
        speed: "?",
        defense: "?",
        spA: "?",
        spD: "?",
      };

  return (
    <Box position="relative" width="200px">
      <Box
        position="absolute"
        top="-60px"
        left="50%"
        transform="translateX(-50%)"
        zIndex={2}
      >
        {pokemon ? (
          <Box
            width="40px"
            height="40px"
            borderRadius="full"
            bg="gray.200"
            display="flex"
            alignItems="center"
            justifyContent="center"
          >
            <CheckIcon />
          </Box>
        ) : (
          <Box
            width="40px"
            height="40px"
            borderRadius="full"
            bg="gray.200"
            display="flex"
            alignItems="center"
            justifyContent="center"
            cursor="pointer"
            onClick={() => setIsOpen(true)}
          >
            <AddIcon />
          </Box>
        )}
      </Box>

      {/* Main Pokémon Card */}
      <Box
        bg={pokemon ? "yellow.400" : "white"}
        borderRadius="lg"
        px={5}
        py={5}
        boxShadow="md"
      >
        {loading ? (
          <Spinner />
        ) : (
          <VStack spacing={4} align="stretch">
            <Text fontSize="xl" fontWeight="bold" textAlign="center">
            {pokemon ? pokemon.name : "Choose Your Pokémon"}
            </Text>

            {pokemon && (
              <HStack justify="center" spacing={2} mb={2}>
                <Tag colorScheme="gray" size="md">
                  {pokemon.type1}
                </Tag>
                {pokemon.type2 && (
                  <Tag colorScheme="gray" size="md">
                    {pokemon.type2}
                  </Tag>
                )}
              </HStack>
            )}

            <StatRow label="HP" value={stats.hp} />
            <StatRow label="ATK" value={stats.attack} />
            <StatRow label="SPD" value={stats.speed} />
            <StatRow label="DEF" value={stats.defense} />
            <StatRow label="S.ATK" value={stats.spA} />
            <StatRow label="S.DEF" value={stats.spD} />

            {/* Display selected moves */}
            {selectedMoves.length > 0 && (
              <VStack align="start" spacing={1}>
                <Text fontWeight="bold" fontSize="lg">
                  Moves
                </Text>
                {selectedMoves.map((move, idx) => (
                  <Text key={idx} fontSize="md" pl={2}>
                    {move}
                  </Text>
                ))}
              </VStack>
            )}

            {/* Display selected nature */}
            {selectedNature && (
              <VStack align="start" spacing={1}>
                <Text fontWeight="bold" fontSize="lg">
                  Nature
                </Text>
                <Text fontSize="md" pl={2}>
                  {selectedNature}
                </Text>
              </VStack>
            )}

            {/* Display selected ability */}
            {selectedAbility && (
              <VStack align="start" spacing={1}>
                <Text fontWeight="bold" fontSize="lg">
                  Ability
                </Text>
                <Text fontSize="md" pl={2}>
                  {selectedAbility}
                </Text>
              </VStack>
            )}
          </VStack>
        )}
      </Box>

      {/* Modal for search and multi-step selection */}
      <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} isCentered>
        <ModalOverlay />
        <ModalContent bg="purple.100" minW="500px">
          <ModalBody p={4}>
            <Heading size="md" my={5} textAlign="center">
              {currentStep === 1 && "Choose Your Pokémon"}
              {currentStep === 2 && `Select Moves for ${pokemon?.name}`}
              {currentStep === 3 && "Select Nature"}
              {currentStep === 4 && "Select Ability"}
            </Heading>

            {/* Step 1: Search Pokémon */}
            {currentStep === 1 && (
              <>
                <SearchBar handleSearch={handleSearch} />
                <VStack align="stretch" spacing={2} mb={3}>
                  {searchResults.length > 0 ? (
                    searchResults.map((p) => (
                      <HStack
                        key={p.id}
                        p={3}
                        bg="white"
                        borderRadius="md"
                        cursor="pointer"
                        _hover={{ bg: "gray.100" }}
                        onClick={() => fetchPokemon(p.id)}
                      >
                        <Text>{p.name}</Text>
                      </HStack>
                    ))
                  ) : (
                    <Text fontSize="lg">No Pokémon found</Text>
                  )}
                </VStack>
              </>
            )}

            {/* Step 2: Select Moves */}
            {currentStep === 2 && pokemon && (
              <>
                <Text>Select up to 4 moves:</Text>
                <Wrap>
                  {pokemon.moves.map((move) => (
                    <Tag
                      key={move}
                      colorScheme={
                        selectedMoves.includes(move) ? "blue" : "gray"
                      }
                      cursor="pointer"
                      onClick={() => handleMoveSelection(move)}
                    >
                      {move}
                    </Tag>
                  ))}
                </Wrap>
                <Button
                  mt={4}
                  onClick={() => setCurrentStep(3)}
                  isDisabled={selectedMoves.length === 0}
                >
                  Next
                </Button>
              </>
            )}

            {/* Step 3: Select Nature */}
            {currentStep === 3 && (
              <>
                <Text>Select a nature:</Text>
                <Wrap>
                  {natures.map((nature) => (
                    <Tag
                      key={nature}
                      colorScheme={selectedNature === nature ? "blue" : "gray"}
                      cursor="pointer"
                      onClick={() => setSelectedNature(nature)}
                    >
                      {nature}
                    </Tag>
                  ))}
                </Wrap>
                <Button
                  mt={4}
                  onClick={() => setCurrentStep(4)}
                  isDisabled={!selectedNature}
                >
                  Next
                </Button>
              </>
            )}

            {/* Step 4: Select Ability */}
            {currentStep === 4 && pokemon && (
              <>
                <Text>Select an ability:</Text>
                <Wrap>
                  {pokemon.abilities.map((ability) => (
                    <Tag
                      key={ability}
                      colorScheme={
                        selectedAbility === ability ? "blue" : "gray"
                      }
                      cursor="pointer"
                      onClick={() => setSelectedAbility(ability)}
                    >
                      {ability}
                    </Tag>
                  ))}
                </Wrap>
                <Button
                  mt={4}
                  colorScheme="green"
                  onClick={confirmAddToTeam}
                  isDisabled={!selectedAbility}
                >
                  Confirm Selection
                </Button>
              </>
            )}
          </ModalBody>
        </ModalContent>
      </Modal>
    </Box>
  );
}

export default PokemonCard;
