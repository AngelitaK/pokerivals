import React from 'react';
import { Box, Text, Flex, Badge } from "@chakra-ui/react";

// Helper function to format the date
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const day = date.getDate();
  const month = date.toLocaleString('default', { month: 'long' }); 
  return `${day} ${month}`;
};

// Helper function to format the time
const formatTime = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const TransactionItem = ({ transaction }) => {
  const { transactionTime, changeInCents, pending, cancelled } = transaction;

  // Determine the status and color
  const status = pending ? "Pending" : cancelled ? "Cancelled" : "Successful";
  const statusColor = pending ? "orange" : cancelled ? "red" : "green";

  return (
    <Box
      p={4}
      bg="white"
      borderRadius="lg"
      boxShadow="md"
      mb={4}
      width="100%"
      maxWidth="100%"
    >
      <Flex align="center" justify="space-between" gap={3}>

        {/* Date */}
        <Text fontSize="md" color="gray.600">
          {formatDate(transactionTime)}
        </Text>

        {/* Time */}
        <Text fontSize="md" color="gray.600">
          {formatTime(transactionTime)}
        </Text>

        {/* Amount */}
        <Text fontSize="lg" fontWeight="bold" color="gray.800">
        🪙{changeInCents}
        </Text>

        {/* Status Badge */}
        <Badge colorScheme={statusColor} fontSize="sm" px={3} py={1} borderRadius="md">
          {status}
        </Badge>
      </Flex>
    </Box>
  );
};

export default TransactionItem;
