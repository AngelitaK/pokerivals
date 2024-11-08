"use client"
import { Flex, Text, Button, Icon, Stack } from "@chakra-ui/react";
import { FaCoins } from "react-icons/fa";

const BettingComponent = ({ player1, player2, odds1, odds2, bettingPool, payout, yourBet, isResult }) => {
    return (
        <Flex
            bg="whiteAlpha.800"
            borderRadius="md"
            p={4}
            align="center"
            justify="space-between"
            mb={4}
            boxShadow="lg"
            border="1px solid"
            borderColor="gray.300"
            minH="100px"
            _hover={{ bg: "whiteAlpha.900", transform: "scale(1.02)", transition: "0.2s" }}
            transition="all 0.2s ease-in-out"
        >
            <Stack spacing={1} align="center" width="20%">
                <Text fontWeight="bold" fontSize="lg" color="gray.700">{player1}</Text>
                <Text fontSize="sm" color="gray.600">Odds: {odds1}</Text>
                {isResult && yourBet === player1 && <Text color="green.500" fontWeight="bold">Winner</Text>}
            </Stack>

            <Text fontWeight="bold" fontSize="2xl" color="red.500">
                VS
            </Text>

            <Stack spacing={1} align="center" width="20%">
                <Text fontWeight="bold" fontSize="lg" color="gray.700">{player2}</Text>
                <Text fontSize="sm" color="gray.600">Odds: {odds2}</Text>
                {isResult && yourBet === player2 && <Text color="green.500" fontWeight="bold">Winner</Text>}
            </Stack>

            {isResult ? (
                <>
                    <Flex align="center" width="15%">
                        <Text fontSize="lg" fontWeight="bold" color="gray.700">{yourBet}</Text>
                    </Flex>
                    <Flex align="center" width="15%">
                        <Icon as={FaCoins} color="yellow.400" boxSize={5} />
                        <Text ml={2} fontWeight="bold" fontSize="lg" color="gray.700">{bettingPool}</Text>
                    </Flex>
                    <Flex align="center" width="15%">
                        <Icon as={FaCoins} color="yellow.400" boxSize={5} />
                        <Text ml={2} fontWeight="bold" fontSize="lg" color={payout > 0 ? "green.500" : "gray.700"}>
                            {payout}
                        </Text>
                    </Flex>
                </>
            ) : (
                <>
                    <Flex align="center" width="15%">
                        <Icon as={FaCoins} color="yellow.400" boxSize={5} />
                        <Text ml={2} fontWeight="bold" fontSize="lg" color="gray.700">{bettingPool}</Text>
                    </Flex>
                    <Button colorScheme="teal" size="md">Bet</Button>
                </>
            )}
        </Flex>
    );
};

export default BettingComponent;