"use client"
import React from "react";
import {
    Box,
    Flex,
    Text,
    Button,
    Modal,
    ModalOverlay,
    ModalContent,
    ModalHeader,
    ModalCloseButton,
    ModalBody,
    ModalFooter,
    Grid
} from "@chakra-ui/react";

const PlayerProfileModal = ({ isOpen, onClose, playerData }) => {
    return (
        <Modal isOpen={isOpen} onClose={onClose} size="lg">
            <ModalOverlay />
            <ModalContent>
                <ModalHeader>{playerData.playerUsername}'s Team Composition</ModalHeader>
                <ModalCloseButton />
                <ModalBody>
                    <Grid templateColumns="repeat(2, 1fr)" gap={4}>
                        {playerData.chosenPokemons.map((pokemon, index) => (
                            <Box
                                key={index}
                                p={3}
                                border="1px"
                                borderColor="gray.200"
                                borderRadius="md"
                                boxShadow="sm"
                            >
                                <Text fontSize="lg" fontWeight="bold" color="blue.600">
                                    {pokemon.pokemon.name} ({pokemon.nature})
                                </Text>
                                <Text fontSize="sm" color="gray.500" mb={2}>
                                    Ability: {pokemon.ability}
                                </Text>
                                <Text fontSize="sm" fontWeight="bold">Stats:</Text>
                                <Flex fontSize="sm" mb={2} wrap="wrap">
                                    <Box mr={2}>HP: {pokemon.pokemon.stats.hp}</Box>
                                    <Box mr={2}>Attack: {pokemon.pokemon.stats.attack}</Box>
                                    <Box mr={2}>Defense: {pokemon.pokemon.stats.defense}</Box>
                                    <Box mr={2}>Speed: {pokemon.pokemon.stats.speed}</Box>
                                    <Box mr={2}>SpA: {pokemon.pokemon.stats.spA}</Box>
                                    <Box>SpD: {pokemon.pokemon.stats.spD}</Box>
                                </Flex>
                                <Text fontSize="sm" fontWeight="bold">Moves:</Text>
                                <Flex fontSize="sm" flexWrap="wrap">
                                    {pokemon.moves.map((move, moveIndex) => (
                                        <Text key={moveIndex} mr={2} color="gray.600">
                                            {move}
                                        </Text>
                                    ))}
                                </Flex>
                            </Box>
                        ))}
                    </Grid>
                </ModalBody>
                <ModalFooter>
                    <Button onClick={onClose}>Close</Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
};

export default PlayerProfileModal;