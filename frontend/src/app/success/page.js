'use client'

import { Box, Heading, Text, Button } from '@chakra-ui/react'
import { CheckCircleIcon } from '@chakra-ui/icons'
import { useRouter } from 'next/navigation'

export default function Success() {
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
        <CheckCircleIcon boxSize={'60px'} color={'green.600'} />
        <Heading as="h2" size="xl" mt={6} mb={2} color="blue.800" fontFamily="Pokemon, sans-serif">
          Top Up Successful!
        </Heading>
        <Text fontSize="lg" color="gray.700" fontFamily="sans-serif" mb={2}>
          Your journey continues with more PokeCredits!
        </Text>
        <Text fontSize="lg" color="gray.700" fontFamily="sans-serif" mb={4}>
          Battle more, earn more!
        </Text>
        <Button
          colorScheme="red"
          bg="green.400"
          _hover={{ bg: 'green.500' }}
          onClick={() => router.push('/top-up')}
        >
          Return to Main Page
        </Button>
      </Box>
    </Box>
  )
}
