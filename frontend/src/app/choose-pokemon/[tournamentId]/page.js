"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "../../../../config/axiosInstance";
import useAuth from "../../../../config/useAuth";
import LoadingOverlay from "../../../components/loadingOverlay";
import PokemonCard from "@/components/pokemonCard";
import {
  Flex,
  Heading,
  Text,
  Button,
  useToast
} from "@chakra-ui/react";

const ChoosePokemon = ({ params }) => {
  const router = useRouter();
  const toast = useToast();

  const [finalTeam, setFinalTeam] = useState([]); // Use an empty array for the team

  const { isAuthenticated, loading } = useAuth("PLAYER");
  console.log(isAuthenticated, loading);

  const { tournamentId } = params;

  // Function to add Pokémon to the final team
  const addPokemonToTeam = (pokemon) => {
    if (finalTeam.length < 7) { // Limit to 6 Pokémon
      setFinalTeam((prevTeam) => [...prevTeam, pokemon]);
    } else {
      console.warn("Team is already full");
    }
  };

   // Log finalTeam whenever it updates
   useEffect(() => {
    console.log("Updated team:", finalTeam);
    console.log(tournamentId);
  }, [finalTeam, tournamentId]);

  // Handle posting the final team to the API
  const handleReadyForBattle = async () => {
    console.log("Final team:", finalTeam);
    
    const pokemonChoicesRaw = finalTeam.map(pokemon => ({
      pokemonId: pokemon.id,
      moves: pokemon.moves,
      nature: pokemon.nature,
      ability: pokemon.ability
   }));

    try {
      const response = await axios.post(`/player/tournament/${tournamentId}/join`, {
        pokemonChoicesRaw: pokemonChoicesRaw

      }
    );
      console.log("Successfully joined tournament:", response.data);
      
      toast({
        title: "Successfully joined tournament!",
        status: "success",
        duration: 5000,
        isClosable: true
      });

      // Redirect to /find-tournament after showing the success message
      await router.push("/find-tournament");

    } catch (error) {
      console.error("Error joining tournament:", error);
      toast({
        title: "Error joining tournament!" + response.data.explanation,
        status: "error",
        duration: 5000,
        isClosable: true
      });
    }
  };


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

      {/* Render PokémonCard and pass addPokemonToTeam as a prop */}
       <Flex gap={8} my={10} pt={10} wrap="wrap" justify="center">
      {[...Array(6)].map((_, index) => (
        <PokemonCard key={index} addToTeam={addPokemonToTeam} />
      ))}
    </Flex>

      <Button mt={8} colorScheme="blue" size="lg" borderRadius={"lg"} minW={"500px"} minH={"60px"}  onClick={handleReadyForBattle} >
        <Text fontSize={"2xl"}>Ready for Battle!</Text>
      </Button>
    </Flex>
  );
};

export default ChoosePokemon;