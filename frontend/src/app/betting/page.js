"use client";

import { useEffect, useState } from "react";
import { Box, Flex, Button, Text, useToast } from "@chakra-ui/react";
import axios from "../../../config/axiosInstance";
import BettingComponent from "@/components/bettingComponent";
import { useRouter } from "next/navigation";
import useAuth from "../../../config/useAuth";
import LoadingOverlay from "../../components/loadingOverlay";

const limit = 5;
const start = new Date().toISOString();

var end = new Date();
end.setMonth(end.getMonth() + 1)
end = end.toISOString();

const BettingPage = () => {
    const router = useRouter();

    const [matches, setMatches] = useState(null);
    const [page, setPage] = useState(0);
    const [count, setCount] = useState(0);

    const toast = useToast();

    // Check authentication
   const { isAuthenticated, user, loading } = useAuth("PLAYER");
   console.log(isAuthenticated, user, loading);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await axios.get('/tournament/match/timing', { params: { start, end, page, limit } });

                if (response.status !== 200) {
                    throw new Error("Failed to fetch matches");
                }

                const data = response.data;

                setMatches(data.matches);
                setCount(data.count);
            } catch (error) {
                console.error("Error fetching matches: ", error);
                toast({
                    title: "Error",
                    description: "Failed to load bet results." + data.explanation,
                    status: "error",
                    duration: 3000,
                    isClosable: true,
                });
            }
        };

        fetchMatches();
    }, [page]);

    const totalPages = Math.ceil(count / limit);

    if (loading) return <LoadingOverlay />;
    if (!isAuthenticated) return null;

    return (
        <Box p={[4, 6, 8]} minH="100vh" backgroundImage="url('/PokeRivalsBackgroundBetting.jpg')" backgroundSize="cover" backgroundPosition="center" bgopacity="0.8">
            <Box bg="whiteAlpha.800" borderRadius="md" p={[2, 4]} maxW="800px" mx="auto">
                <Flex justifyContent="center" alignItems="center" mb={4} position="relative">
                    <Text fontWeight="bold" fontSize={["md", "lg", "xl"]} color="blue.600" textAlign="center">
                        Ongoing Matches
                    </Text>
                    <Button
                        onClick={() => router.push('/top-up')}
                        position="absolute"
                        right={[2, 4, 6]}
                        fontSize={["sm", "md"]}
                    >
                        🪙 Top Up
                    </Button>
                </Flex>
                <Box>
                    {matches && matches.map((match, index) => (
                        <BettingComponent key={index} details={match} />
                    ))}
                    <Flex justify="center" mt={4} wrap="wrap">
                        <Button
                            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                            isDisabled={page === 0}
                            mx={2}
                            mb={[2, 0]}
                            fontSize={["sm", "md"]}
                        >
                            Previous
                        </Button>
                        <Text fontSize={["xs", "sm", "md"]} alignSelf="center" mb={[2, 0]}>
                            {`Page ${totalPages === 0 ? 0 : page + 1} of ${totalPages}`}
                        </Text>
                        <Button
                            onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                            isDisabled={page === totalPages - 1}
                            mx={2}
                            fontSize={["sm", "md"]}
                        >
                            Next
                        </Button>
                    </Flex>
                </Box>
            </Box>
        </Box>
    );
};

export default BettingPage;
