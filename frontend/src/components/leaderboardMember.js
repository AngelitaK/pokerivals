import { Box, Flex, Text, Badge } from "@chakra-ui/react";

export default function LeaderboardMember({ player, clan, points }) {
  return (
    <Flex
      bg="whiteAlpha.800"
      borderRadius="md"
      p={6}
      align="center"
      justify="space-between"
      mb={4}
      boxShadow="lg"
      border="1px solid"
      borderColor="gray.300"
      _hover={{
        bg: "whiteAlpha.900",
        transform: "scale(1.02)",
        transition: "0.2s",
      }}
      transition="all 0.2s ease-in-out"
    >
      <Box width="100%" color={"black"}>
        <Flex alignItems="center" justifyContent="space-between" gap={2} position="relative">
          <Text fontWeight="bold" fontSize="xl" mx={5}>
            {player}
          </Text>

          <Badge
            fontSize="lg"
            colorScheme="purple"
            py={3}
            px={5}
            borderRadius="full"
            position="absolute"
            left="50%" 
            transform="translateX(-50%)" 
          >
            {clan || 'Clanless'}
          </Badge>

          <Text fontSize="lg" mx={5}>
            {points.toFixed(0)}
          </Text>
        </Flex>
      </Box>
    </Flex>
  );
}