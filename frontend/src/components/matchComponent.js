"use client"
import React, { useState, useRef } from 'react';
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
} from '@chakra-ui/react';
import axios from '../../config/axiosInstance';

const MatchComponent = ({ seed, toast }) => {
  const [proposedTime, setProposedTime] = useState(seed.timeMatchOccurs || '');
  const [selectedWinner, setSelectedWinner] = useState(seed.matchResult || '');
  const { isOpen, onOpen, onClose } = useDisclosure();
  const cancelRef = useRef();

  const bothPlayersAccepted = seed.team_a_agree_timing === 'ACCEPTED' && seed.team_b_agree_timing === 'ACCEPTED';

  const handleProposeTime = () => {
    if (!proposedTime) {
      toast({
        title: 'Invalid Time',
        description: 'Please enter a valid proposed time.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    axios.post('http://localhost:8080/tournament/match/timing', {
      matchId: {
        tournamentId: seed.tournament_id,
        depth: seed.depth,
        index: seed.index,
      },
      approve: true,
    })
      .then(() => {
        toast({
          title: 'Time Proposed',
          description: `Proposed time for match ${seed.index + 1} has been updated.`,
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
      })
      .catch(error => console.error('Error proposing time:', error));
  };

  const handleConfirmResult = () => {

    axios.patch('http://localhost:8080/tournament/match/result', {
      matchId: {
        tournamentId: seed.tournament_id,
        depth: seed.depth,
        index: seed.index,
      },
      matchResult: (selectedWinner === seed.teams[0].id ? "TEAM_A" : "TEAM_B"),
    })
      .then(() => {
        toast({
          title: 'Result Updated',
          description: `Result for match ${seed.index + 1} has been updated.`,
          status: 'success',
          duration: 3000,
          isClosable: true,
        });
        onClose(); // Close the modal after submission
      })
      .catch(error => console.error('Error updating result:', error));
  };

  const handleSubmitClick = () => {
    if (!selectedWinner) {
      toast({
        title: 'No Winner Selected',
        description: 'Please select a winner before submitting.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
      return;
    }
    onOpen(); // Open the confirmation modal
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
          <Text>Proposed Time: {proposedTime ? new Date(proposedTime).toLocaleString() : 'Not Set'}</Text>
          <Text>{seed.teams[0].id} Status: {seed.team_a_agree_timing}</Text>
          <Text>{seed.teams[1].id} Status: {seed.team_b_agree_timing}</Text>
          <Text>Overall Status: {seed.both_agree_timing}</Text>
          <HStack>
            <Input
              placeholder="Propose Time"
              value={proposedTime}
              onChange={(e) => setProposedTime(e.target.value)}
              size="sm"
              type="datetime-local"
              isDisabled={bothPlayersAccepted}
            />
            <Button
              colorScheme="blue"
              onClick={handleProposeTime}
              isDisabled={bothPlayersAccepted}
              width={'fit-content'}
            >
              Propose Time
            </Button>
          </HStack>
          <Select
            placeholder={seed.can_set_result ? "Select Winner" : seed.matchResult}
            size="sm"
            value={selectedWinner}
            onChange={(e) => setSelectedWinner(e.target.value)}
            isDisabled={!seed.can_set_result}
          >
            <option value={seed.teams[0].id}>{seed.teams[0].id}</option>
            <option value={seed.teams[1].id}>{seed.teams[1].id}</option>
          </Select>
          <Button
            colorScheme="green"
            onClick={handleSubmitClick}
            isDisabled={!seed.can_set_result}
            mt={2}
          >
            Submit
          </Button>
        </VStack>

        {/* Confirmation Modal */}
        <AlertDialog
          isOpen={isOpen}
          leastDestructiveRef={cancelRef}
          onClose={onClose}
        >
          <AlertDialogOverlay>
            <AlertDialogContent>
              <AlertDialogHeader fontSize="lg" fontWeight="bold">
                Confirm Result
              </AlertDialogHeader>

              <AlertDialogBody>
                Are you sure you want to submit the result with <b>{selectedWinner}</b> as the winner?
              </AlertDialogBody>

              <AlertDialogFooter>
                <Button colorScheme="red" ref={cancelRef} onClick={onClose}>
                  Cancel
                </Button>
                <Button colorScheme="green" onClick={handleConfirmResult} ml={3}>
                  Confirm
                </Button>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialogOverlay>
        </AlertDialog>
      </fieldset>
    </Box>
  );
}

export default MatchComponent;
