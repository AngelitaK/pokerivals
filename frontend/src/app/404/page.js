'use client'

import { Box, Heading, Text, Button } from '@chakra-ui/react'
import { CheckCircleIcon } from '@chakra-ui/icons'
import { useRouter } from 'next/navigation'

export default function NotFound() {
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
        <Heading as="h2" size="3xl" mt={6} mb={2} color="blue.800" fontFamily="Pokemon, sans-serif">
          404
        </Heading>
        <Text fontSize="xl" color="gray.700" fontFamily="sans-serif" mb={2}>
          The page you are looking for is not found.
        </Text>
        <Text fontSize="xl" color="gray.700" fontFamily="sans-serif" mb={4}>
          Get back out there, and battle!
        </Text>
        <Button
          colorScheme="red"
          bg="blue.800"
          _hover={{ bg: 'blue.700' }}
          onClick={() => router.push('/find-tournament')}
        >
          Back to Main Page
        </Button>
      </Box>
    </Box>
  )
}
