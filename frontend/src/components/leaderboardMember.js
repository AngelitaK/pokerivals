import {
  Box,
  Flex,
  Text,
  Badge,
} from "@chakra-ui/react";

export default function LeaderboardMember() {
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
      <Box width="100%" color={"black"} >
        <Flex
          alignItems="center"
          justifyContent="space-between"
          gap={2}
        >
          <Text fontWeight="bold" fontSize="xl" ml={5}>
            NoobMaster69
          </Text>

          <Badge
            fontSize="lg"
            colorScheme="purple"
            p={2}
            borderRadius="full"
            mr={4}
          >
            Team Rocket
          </Badge>
         
          <Text fontSize="lg" mr={5}>
            5291
          </Text>
        </Flex>
      </Box>
    </Flex>
  );
}