"use client"
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "../../../config/axiosInstance";
import {
    Box,
    Flex,
    Text,
    Button,
    HStack,
    useToast
} from "@chakra-ui/react";
import {
    FaArrowCircleLeft,
    FaCalendar,
} from "react-icons/fa";

const pageSize = 3

const convertToSGT = (utcDateStr) => {
    const utcDate = new Date(utcDateStr);
    utcDate.setHours(utcDate.getHours() + 8); // Convert to Singapore Time (UTC+8)
    return utcDate.toISOString();
};

function formatDate(dateString) {
    const date = new Date(dateString);

    // Options for formatting the date and time
    const options = {
        day: "numeric",
        month: "short",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
        hour12: true,
        timeZone: "UTC"
    };

    return new Intl.DateTimeFormat("en-GB", options).format(date);
}

const ListComponent = ({ details }) => {
    const router = useRouter();

    sessionStorage.setItem('name', details.name);
    sessionStorage.setItem('tournamentBegin', details.estimatedTournamentPeriod.tournamentBegin);
    sessionStorage.setItem('tournamentEnd', details.estimatedTournamentPeriod.tournamentEnd);

    const handleNavigation = () => {
        router.push(`/manage-tournament/manage-player?id=${details.id}`);
    }

    return (
        <>
            <Flex
                ml={'3%'}
                mt={'2%'}
                alignItems={'center'}
                justifyContent={'space-between'}>
                <Box>
                    <Text fontSize={'xl'} fontWeight={'semibold'}>{details.name}</Text>
                    <Text fontSize={'sm'}><span style={{ fontWeight: "bold" }}>From:</span> {formatDate(convertToSGT(details.estimatedTournamentPeriod.tournamentBegin))}</Text>
                    <Text fontSize={'sm'}><span style={{ fontWeight: "bold" }}>End:</span> {formatDate(convertToSGT(details.estimatedTournamentPeriod.tournamentEnd))}</Text>
                </Box>
                <Button
                    mr={'3%'}
                    backgroundColor={'deepskyblue'}
                    onClick={handleNavigation}>
                    <Text
                        color={'white'}>Manage</Text>
                </Button>
            </Flex>
        </>
    )
}

const ManageTournamentPage = () => {
    const router = useRouter();
    const [page, setPage] = useState(0);
    const [tournaments, setTournaments] = useState([]);
    const [count, setCount] = useState(0);
    const toast = useToast();

    const handleBackNavigation = () => {
        router.push('/admin-home');
    }

    const handleCalendarNavigation = () => {
        router.push("/calendar");
    }

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/admin/tournament/me?page=${page}&limit=${pageSize}`);
                console.log(response)
                if (response.status !== 200) {
                    throw new Error("Failed to fetch tournaments");
                }

                const data = response.data;
                setTournaments(data.tournaments);
                setCount(data.count);

                sessionStorage.setItem("tournaments", JSON.stringify(data.tournaments));

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
            <Box>
                <Flex
                    align={'center'}
                    margin={'1% 0% 2% 2%'}
                    position={'relative'}
                >
                    <Flex align={"center"} onClick={handleBackNavigation}>
                        <FaArrowCircleLeft size={"4vh"} />
                        <Text ml={"1vh"} fontSize={"3xl"}>
                            Back
                        </Text>
                    </Flex>

                    <Text fontSize={'3xl'} margin={'auto'} fontWeight={'bold'} left={'50%'}
                        transform={"translateX(-20%)"}>
                        Manage Tournaments
                    </Text>
                </Flex>
            </Box>

            <Flex
                justifyContent={'space-around'}>
                <Box
                    backgroundColor={'lightgrey'}
                    borderRadius={'5%'}
                    width={'45vw'}
                    height={'70vh'}
                    mb={'2%'}
                    position={'relative'}>
                    <Flex
                        justifyContent={'space-between'}
                        alignContent={'center'}>
                        <Text fontSize={'2xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'}>Tournament Created</Text>
                        {/* <Button mr={'3%'} mt={'3%'} onClick={handleCalendarNavigation}>
                            <Text mr={'5%'}>View Calendar</Text>
                            <Box>
                                <FaCalendar
                                    width={'5vw'}
                                    height={'5vh'} />
                            </Box>
                        </Button> */}
                    </Flex>
                    <Flex
                        justifyContent={'space-between'}>
                        <Text fontSize={'xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'}>Details</Text>
                        <Text fontSize={'xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'} mr={'3%'}>Actions</Text>
                    </Flex>
                    {tournaments.map((tournament) => 
                        <Box><ListComponent key={tournament.id} details={tournament} path={tournament.id} /></Box>
                    )}
                    <Box
                        position={'absolute'}
                        bottom={'3%'}
                        left={'50%'}
                        transform={"translateX(-50%)"}>
                        <HStack justifyContent="center" mt={4}>
                            <Button
                                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                                isDisabled={page === 0}
                            >
                                Previous
                            </Button>
                            <Text fontSize={'sm'}>{`Page ${totalPages === 0 ? 0 : page + 1} of ${totalPages}`}</Text>
                            <Button
                                onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                                isDisabled={page === 0 ? true : page === totalPages - 1}
                            >
                                Next
                            </Button>
                        </HStack>
                    </Box>
                </Box>

                <Box
                    backgroundColor={'lightgrey'}
                    borderRadius={'5%'}
                    width={'45vw'}
                    height={'70vh'}
                    mb={'2%'}
                    position={'relative'}>
                    <Text fontSize={'2xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'}>Tournament Ongoing</Text>
                    <Flex
                        justifyContent={'space-between'}>
                        <Text fontSize={'xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'}>Details</Text>
                        <Text fontSize={'xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'} mr={'3%'}>Actions</Text>
                    </Flex>
                    {tournaments.map((tournament) => 
                        <Box><ListComponent key={tournament.id} details={tournament} /></Box>
                    )}
                    <Box
                        position={'absolute'}
                        bottom={'3%'}
                        left={'50%'}
                        transform={"translateX(-50%)"}>
                        <HStack justifyContent="center" mt={4}>
                            <Button
                                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                                isDisabled={page === 0}
                            >
                                Previous
                            </Button>
                            <Text fontSize={'sm'}>{`Page ${totalPages === 0 ? 0 : page + 1} of ${totalPages}`}</Text>
                            <Button
                                onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                                isDisabled={page === 0 ? true : page === totalPages - 1}
                            >
                                Next
                            </Button>
                        </HStack>
                    </Box>
                </Box>
            </Flex>
        </>
    )

}

export default ManageTournamentPage;