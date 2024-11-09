'use client'

import { Box, Heading, Text, Button } from '@chakra-ui/react'
import { WarningTwoIcon } from '@chakra-ui/icons'
import { useRouter } from 'next/navigation'

export default function Cancel() {
  const router = useRouter()

  return (
    <Box
      height="100vh"
      backgroundImage="url('/TopupBG.png')"
      backgroundPosition="center"
      backgroundSize="cover"
      display="flex"
      alignItems="center"
      justifyContent="center"
    >
      <Box
        bg="white"
        boxShadow="lg"
        borderRadius="md"
        textAlign="center"
        p={10}
        minW="lg"
      >
        <WarningTwoIcon boxSize={'60px'} color={'red.600'} />
        <Heading as="h2" size="xl" mt={6} mb={2} color="blue.800" fontFamily="Pokemon, sans-serif">
          Top Up Cancelled
        </Heading>
        <Text fontSize="lg" color="gray.700" fontFamily="sans-serif" mb={4}>
          Please try again to get 'em PokeCredits!
        </Text>
        <Button
          colorScheme="red"
          bg="red.400"
          _hover={{ bg: 'red.500' }}
          onClick={() => router.push('/top-up')}
        >
          Return to Main Page
        </Button>
      </Box>
    </Box>
  )
}
