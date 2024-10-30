import { Box, Flex, Text, Button } from "@chakra-ui/react";

export default function FindTournament() {
  return (
    <Flex
      justify="space-between"
      align="center"
      bg="rgba(255, 255, 255, 0.8)"
      p={4}
      borderRadius="xl"
      mb={4}
      width='100%'
    >

      <Text fontWeight="bold" w="180px" color="black">Master Battle</Text>
      <Text color="black">Mon, 31 Sep 2024</Text>
      <Text color="black">12:00PM - 3:00PM</Text>
      <Text color="black">900</Text>
      <Text color="black">1200</Text>
      <Button colorScheme="teal">
        Manage
      </Button>
    </Flex>
  );
}