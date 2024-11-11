import React, { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
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

const MatchComponent = ({ seed, toast, onReload }) => {
  const router = useRouter()
  const [proposedTime, setProposedTime] = useState(seed.timeMatchOccurs || '');
  const [enteredTime, setEnteredTime] = useState("")
  const [selectedPlayer, setSelectedPlayer] = useState(seed.matchResult || '');
  const [selectedPlayerType, setSelectedPlayerType] = useState("")
  const { isOpen, onOpen, onClose } = useDisclosure();
  const cancelRef = useRef();

  const handleProposeTime = () => {
    if (!enteredTime) {
      toast({
        title: 'Invalid Time',
        description: 'Please enter a valid proposed time.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    axios.post('/tournament/match/timing', {
      matchId: {
        tournamentId: seed.tournament_id,
        depth: seed.depth,
        index: seed.index,
      },
      matchTiming: new Date(enteredTime).toISOString(),
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

    onReload();
  };

  const handleConfirmResult = () => {

    if (selectedPlayerType == "winner") {
      axios.patch('/tournament/match/result', {
        matchId: {
          tournamentId: seed.tournament_id,
          depth: seed.depth,
          index: seed.index,
        },
        matchResult: (selectedPlayer === seed.teams[0].id ? "TEAM_A" : "TEAM_B"),
      })
        .then(() => {
          toast({
            title: 'Result Updated',
            description: `Result for match ${seed.index + 1} has been updated.`,
            status: 'success',
            duration: 3000,
            isClosable: true,
          });
          onClose();
        })
        .catch(error => console.error('Error updating result:', error));
    } else {
      axios.delete('/tournament/match/forfeit', {
        data: {
          matchId: {
            tournamentId: seed.tournament_id,
            depth: seed.depth,
            index: seed.index,
          },
          forfeitTeamA: (selectedPlayer === seed.teams[0].id ? true : false),
        }
      })
        .then(() => {
          toast({
            title: 'Result Updated',
            description: `Result for match ${seed.index + 1} has been updated.`,
            status: 'success',
            duration: 3000,
            isClosable: true,
          });
          onClose();
        })
        .catch(error => console.error('Error updating result:', error));
    }
    router.refresh()
  };

  const handleSubmitClick = (type) => {
    if (!selectedPlayer) {
      toast({
        title: 'No Winner/Forfeited player Selected',
        description: 'Please select a winner/Forfeited player before submitting.',
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
      return;
    }
    setSelectedPlayerType(type)
    onOpen();
  };

  return (
    <Box p={4} borderWidth="1px" borderRadius="md" boxShadow="md" mb={4} bg="gray.50">
      <HStack
        spacing={4}
        align="start"
        flexDirection={{ base: 'column', md: 'row' }} // Responsive layout: column on small screens, row on larger screens
      >
        {/* Left Column */}
        <VStack align="start" spacing={2} width={{ base: '100%', md: '50%' }}>
          <HStack>
            <Text fontWeight="bold">{seed.teams[0].id}</Text>
            <Text color="red.500">VS</Text>
            <Text fontWeight="bold">{seed.teams[1].id}</Text>
          </HStack>
          <Text>Proposed Time: {proposedTime ? new Date(proposedTime).toLocaleString() : 'Not Set'}</Text>
          <Text>{seed.teams[0].id} Status: {seed.team_a_agree_timing}</Text>
          <Text>{seed.teams[1].id} Status: {seed.team_b_agree_timing}</Text>
          <Text>{seed.matchResult == "PENDING" ? "Overall Status" : "WINNER"}: {seed.matchResult == "PENDING" ? seed.both_agree_timing : seed.matchResult}</Text>
        </VStack>

        {/* Right Column */}
        <VStack align="start" spacing={2} width={{ base: '100%', md: '50%' }}>
          <fieldset disabled={seed.both_agree_timing === "ACCEPTED" || (seed.both_agree_timing !== "REJECTED" && seed.timeMatchOccurs !== null) || seed.matchResult != "PENDING"}>
            <HStack width="100%">

              <Input
                value={enteredTime}
                onChange={(e) => setEnteredTime(e.target.value)}
                size="sm"
                type="datetime-local"
              />
              <Button
                colorScheme="blue"
                onClick={handleProposeTime}
                width="fit-content"
                minWidth="120px"
                padding="0.5rem 1rem"
                minW="120px"
                minH="40px"
              >
                Propose Time
              </Button>
            </HStack>
          </fieldset>
          <Select
            placeholder={seed.can_set_result ? "Select Winner" : seed.matchResult}
            size="sm"
            value={selectedPlayer}
            onChange={(e) => setSelectedPlayer(e.target.value)}
            isDisabled={!seed.can_set_result}
          >
            <option value={seed.teams[0].id}>{seed.teams[0].id}</option>
            <option value={seed.teams[1].id}>{seed.teams[1].id}</option>
          </Select>
          <HStack alignSelf="flex-end">
            <Button
              colorScheme="red"
              onClick={() => handleSubmitClick("forfeit")}
              isDisabled={!seed.can_set_result}
              mt={2}
            >
              Forfeit Player
            </Button>
            <Button
              colorScheme="green"
              onClick={() => handleSubmitClick("winner")}
              isDisabled={!seed.can_set_result}
              mt={2}
            >
              Submit Winner
            </Button>
          </HStack>

        </VStack>
      </HStack>

      {/* Confirmation Modal */}
      <AlertDialog isOpen={isOpen} leastDestructiveRef={cancelRef} onClose={onClose}>
        <AlertDialogOverlay>
          <AlertDialogContent>
            <AlertDialogHeader fontSize="lg" fontWeight="bold">Confirm Result</AlertDialogHeader>
            <AlertDialogBody>
              Are you sure you want to {selectedPlayerType == "forfeit" ? "FORFEIT" : "submit the result with"} <b>{selectedPlayer}</b> {selectedPlayerType == "winner" && "as the WINNER"}?
            </AlertDialogBody>
            <AlertDialogFooter>
              <Button colorScheme="red" ref={cancelRef} onClick={onClose}>Cancel</Button>
              <Button colorScheme="green" onClick={handleConfirmResult} ml={3}>Confirm</Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </Box>
  );
};

export default MatchComponent;