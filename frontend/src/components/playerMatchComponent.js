"use client";
import React, { useState, useRef } from "react";
import axios from "../../config/axiosInstance";
import {
  Box,
  VStack,
  HStack,
  Text,
  Input,
  Button,
  Select,
  useDisclosure,
  AlertDialog,
  AlertDialogOverlay,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogBody,
  AlertDialogFooter,
} from "@chakra-ui/react";

const PlayerMatchComponent = ({ seed, toast }) => {
  const [proposedTime, setProposedTime] = useState(seed.timeMatchOccurs || "");
  const [selectedWinner, setSelectedWinner] = useState(seed.matchResult || "");

  const bothPlayersAccepted =
    seed.team_a_agree_timing === "ACCEPTED" &&
    seed.team_b_agree_timing === "ACCEPTED";

  return (
    <Box
      p={4}
      borderWidth="1px"
      borderRadius="md"
      boxShadow="md"
      mb={4}
      bg="gray.50"
    >
      <fieldset disabled={seed.forfeited}>
        <VStack align="start" spacing={2}>
          <HStack>
            <Text fontWeight="bold">{seed.teams[0].id}</Text>
            <Text color="red.500">VS</Text>
            <Text fontWeight="bold">{seed.teams[1].id}</Text>
          </HStack>
          <Text>
            Proposed Time:{" "}
            {proposedTime ? new Date(proposedTime).toLocaleString() : "Not Set"}
          </Text>
          <Text>
            {seed.teams[0].id} Status: {seed.team_a_agree_timing}
          </Text>
          <Text>
            {seed.teams[1].id} Status: {seed.team_b_agree_timing}
          </Text>
          <Text>Overall Status: {seed.both_agree_timing}</Text>

          {/* add winner here */}
          <Text>Winner: TEAM{" "}{seed.final_winner}</Text>
        </VStack>
      </fieldset>
    </Box>
  );
};

export default PlayerMatchComponent;