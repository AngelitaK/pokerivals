"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "../../../config/axiosInstance";
import {
    Box,
    Flex,
    Text,
    Button,
    useToast,
    Heading,
} from "@chakra-ui/react";
import { FaArrowCircleLeft } from "react-icons/fa";

const pageSize = 5;

function formatDate(dateString) {
    const date = new Date(dateString);

    const options = {
        day: "numeric",
        month: "short",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
        hour12: true,
        timeZone: "Asia/Singapore",
    };

    return new Intl.DateTimeFormat("en-GB", options).format(date);
}

const ListComponent = ({ details }) => {
    const router = useRouter();

    const handleNavigation = () => {
        const route = details.started ? `/manage-tournament/manage-match?id=${details.id}` : `/manage-tournament/manage-player?id=${details.id}`;
        router.push(route);
    };

    return (
        <Flex
            p={{ base: 3, md: 4 }}
            my={{ base: 2, md: 3 }}
            mx={3}
            alignItems="center"
            justifyContent="space-between"
            bgGradient="linear(to-r, #f0f4c3, #c8e6c9)"
            borderRadius="10px"
            boxShadow="sm"
            _hover={{ boxShadow: "md", transform: "scale(1.01)", transition: "0.2s" }}
        >
            <Box>
                <Text fontSize={{ base: "md", md: "lg" }} fontWeight="bold" color="gray.700">{details.name}</Text>
                <Text fontSize="xs" color="gray.600">
                    <strong>From:</strong> {formatDate(details.estimatedTournamentPeriod.tournamentBegin)}
                </Text>
                <Text fontSize="xs" color="gray.600">
                    <strong>End:</strong> {formatDate(details.estimatedTournamentPeriod.tournamentEnd)}
                </Text>
            </Box>

            <Text fontSize={{ base: "sm", md: "md" }} fontWeight="bold" color="gray.700">{details.started ? "Ongoing" : "Pending Start"}</Text>

            <Button
                size="sm"
                backgroundColor="teal.500"
                onClick={handleNavigation}
                _hover={{ bg: "teal.600" }}
                _focus={{ boxShadow: "outline" }}
                transition="background-color 0.2s"
            >
                <Text color="white">Manage</Text>
            </Button>
        </Flex>
    );
};

const ManageTournamentPage = () => {
    const router = useRouter();
    const [page, setPage] = useState(0);
    const [tournaments, setTournaments] = useState([]);
    const [count, setCount] = useState(0);
    const toast = useToast();

    const handleBackNavigation = () => {
        router.push("/admin-home");
    };

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                const response = await axios.get(
                    `/admin/tournament/me?page=${page}&limit=${pageSize}`
                );

                if (response.status !== 200) {
                    throw new Error("Failed to fetch tournaments");
                }

                const data = response.data;
                setTournaments(data.tournaments);
                setCount(data.count);

                sessionStorage.setItem("tournaments", JSON.stringify(tournaments));
                sessionStorage.setItem("page", page)
                sessionStorage.setItem("pageSize", pageSize)
            } catch (error) {
                console.error("Error fetching Tournaments:", error);
                toast({
                    title: "Error",
                    description: "Failed to load tournaments.",
                    status: "error",
                    duration: 3000,
                    isClosable: true,
                });
            }
        };

        fetchTournaments();
    }, [page]);

    const totalPages = Math.ceil(count / pageSize);

    return (
        <>
            <Box p="1%" backgroundColor="gray.100" minH="100vh">
                <Box>
                    <Flex
                        align="center"
                        justify="space-between"
                        margin="1% 0% 2% 2%"
                    >
                        <Flex align="center" onClick={handleBackNavigation} cursor="pointer" flex="1">
                            <FaArrowCircleLeft size="4vh" />
                            <Text ml="1vh" fontSize="3xl">Back</Text>
                        </Flex>
                        <Heading as="h2" size="lg" textAlign="center" fontWeight="bold" color="gray.800" flex="1">
                            Manage Tournaments
                        </Heading>
                        <Box flex="1" />
                    </Flex>
                </Box>

                <Flex
                    direction={{ base: "column", md: "row" }}
                    justifyContent="space-around"
                    p={4}
                    gap={{ base: 6, md: 4 }}
                >
                    {/* Tournament Created Box */}
                    <Box
                        bg="gray.50"
                        borderRadius="16px"
                        boxShadow="2xl"
                        w={{ base: "100%", md: "45%" }}
                        minHeight="70vh"
                        p={4}
                        pb={5}
                        display="flex"
                        flexDirection="column"
                        justifyContent="space-between"
                    >
                        <Box>
                            <Flex justifyContent="space-between" px={2} mb={4}>
                                <Text fontSize="xl" fontWeight="bold" color="gray.600">Details</Text>
                                <Text fontSize="xl" fontWeight="bold" color="gray.600">Status</Text>
                                <Text fontSize="xl" fontWeight="bold" color="gray.600">Actions</Text>
                            </Flex>
                            {tournaments.map((tournament) => (
                                <ListComponent key={tournament.id} details={tournament} />
                            ))}
                        </Box>
                        <Flex justify="center" mt={4}>
                            <Button
                                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                                isDisabled={page === 0}
                                mx={2}
                            >
                                Previous
                            </Button>
                            <Text fontSize="sm" alignSelf="center">{`Page ${totalPages === 0 ? 0 : page + 1} of ${totalPages}`}</Text>
                            <Button
                                onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                                isDisabled={page === totalPages - 1}
                                mx={2}
                            >
                                Next
                            </Button>
                        </Flex>
                    </Box>
                </Flex>
            </Box>
        </>
    );
};

export default ManageTournamentPage;
