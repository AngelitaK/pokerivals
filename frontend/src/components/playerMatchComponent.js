import React, { useState, useEffect } from "react";
import axios from "../../config/axiosInstance";
import {
  Box,
  VStack,
  HStack,
  Text,
  Button,
  useToast,
} from "@chakra-ui/react";

const PlayerMatchComponent = ({ seed, toast }) => {
  const [proposedTime, setProposedTime] = useState(seed.timeMatchOccurs || "");
  const [statusA, setStatusA] = useState(seed.team_a_agree_timing);
  const [statusB, setStatusB] = useState(seed.team_b_agree_timing);
  const [userId, setUserId] = useState(null);
  const [isButtonDisabled, setIsButtonDisabled] = useState(false);

  // Fetch user ID from localStorage
  useEffect(() => {
    const storedUserId = localStorage.getItem("username");
    if (storedUserId) {
      setUserId(storedUserId);
    }
  }, []);

  // Check if the current user is part of this match
  const isPlayerInvolved = userId === seed.teams[0].id || userId === seed.teams[1].id;

  const approveOrRejectTiming = async (approve) => {
    // Disable both buttons immediately after a click
    setIsButtonDisabled(true);

    try {
      const response = await axios.patch("/tournament/match/timing", {
        matchId: {
          tournamentId: seed.tournament_id,
          depth: seed.depth,
          index: seed.index,
        },
        approve,
      });
      
      // Display toast based on approval or rejection
      if (approve) {
        toast({
          title: "Success",
          description: response.data.message,
          status: "success",
          duration: 3000,
          isClosable: true,
        });
      } else {
        toast({
          title: "Timing Rejected",
          description: "Please wait for our administrator to provide you with another timing. Thanks for your patience!",
          status: "info",
          duration: 10000, // 10 seconds
          isClosable: true,
        });
      }

      // Update the appropriate status based on the player’s team
      if (userId === seed.teams[0].id) {
        setStatusA(approve ? "ACCEPTED" : "REJECTED");
      } else if (userId === seed.teams[1].id) {
        setStatusB(approve ? "ACCEPTED" : "REJECTED");
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update match timing status.",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
      setIsButtonDisabled(false);
    }
  };

  return (
    <Box p={4} borderWidth="1px" borderRadius="md" boxShadow="md" mb={4} bg="gray.50">
      <fieldset disabled={seed.forfeited}>
        <VStack align="start" spacing={2}>
          <HStack>
            <Text fontWeight="bold">{seed.teams[0].id}</Text>
            <Text color="red.500">VS</Text>
            <Text fontWeight="bold">{seed.teams[1].id}</Text>
          </HStack>
          <Text>Admin has proposed this timing: {proposedTime ? new Date(proposedTime).toLocaleString() : "Not Set"}</Text>
          <Text>
            Player A ({seed.teams[0].id}) {statusA === "PENDING" ? "has yet to respond." : 
              statusA === "REJECTED" ? "has rejected the proposed time." : 
              "has accepted the match! Get ready for battle!"}
          </Text>
          <Text>
            Player B ({seed.teams[1].id}) {statusB === "PENDING" ? "has yet to respond." : 
              statusB === "REJECTED" ? "has rejected the proposed time." : 
              "has accepted the match! Get ready for battle!"}
          </Text>

          {/* Show buttons only if player is involved, status is pending, and proposedTime is set */}
          {isPlayerInvolved && proposedTime &&
            ((userId === seed.teams[0].id && statusA === "PENDING") || 
             (userId === seed.teams[1].id && statusB === "PENDING")) && (
            <HStack>
              <Button
                colorScheme="green"
                onClick={() => approveOrRejectTiming(true)}
                isDisabled={isButtonDisabled}
              >
                Approve
              </Button>
              <Button
                colorScheme="red"
                onClick={() => approveOrRejectTiming(false)}
                isDisabled={isButtonDisabled}
              >
                Reject
              </Button>
            </HStack>
          )}
        </VStack>
      </fieldset>
    </Box>
  );
};

export default PlayerMatchComponent;