"use client"
import React, { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "../../../../config/axiosInstance";
import {
    Box,
    Flex,
    Text,
    Button,
    HStack,
    useToast,
    FormControl,
    FormLabel,
    Input
} from "@chakra-ui/react";
import {
    FaArrowCircleLeft,
} from "react-icons/fa";
import TournamentForm from "@/components/tournamentForm";

const test_data = {
    data: {
        teams: [
            {
                chosenPokemons: [
                ],
                tournament: {
                    name: "string",
                    id: "5b24c181-12ce-4efd-be88-b9496123603d"
                },
                playerUsername: "marywoodard"
            },
            {
                chosenPokemons: [
                ],
                tournament: {
                    name: "string",
                    id: "5b24c181-12ce-4efd-be88-b9496123603d"
                },
                playerUsername: "samanthabrooks"
            }
        ],
        count: 2,
        mutable: true
    }
}


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

const date = new Date().toISOString();

const ListComponent = ({ details, registrationEnd }) => {
    const router = useRouter();
    const toast = useToast();

    const handleKick = async () => {
        try {
            const response = await axios.delete(`http://localhost:8080/admin/tournament/${details.tournament.id}/team/player/${details.playerUsername}`);

            if (response.status !== 200) {
                throw new Error("Failed to kick player");
            }

        } catch (error) {
            console.error("Error kicking Player:", error);
            toast({
                title: "Error",
                description: "Failed to kick player.",
                status: "error",
                duration: 3000,
                isClosable: true,
            });
        }
    }

    return (
        <>
            <Flex
                ml={'3%'}
                mt={'2%'}
                alignItems={'center'}
                justifyContent={'space-between'}>
                <Box>
                    <Text fontSize={'xl'} fontWeight={'semibold'}>{details.playerUsername}</Text>
                </Box>
                <Button
                    mr={'3%'}
                    backgroundColor={'tomato'}
                    disabled={date < registrationEnd}
                    onClick={handleKick}>
                    <Text color={'white'}>Kick</Text>
                </Button>
            </Flex>
        </>
    )
}

const ManageTeamPage = () => {
    const router = useRouter();
    const [page, setPage] = useState(0);
    const [tournament, setTournament] = useState(null);
    const [teams, setTeams] = useState([]);
    const [count, setCount] = useState(0);
    const toast = useToast();

    const searchParams = useSearchParams()
    const id = searchParams.get('id')

    const [inviteUsername, setInviteUsername] = useState(""); // State to store input value

    const handleInputChange = (e) => {
        setInviteUsername(e.target.value); // Update state on input change
    };

    const handleBackNavigation = () => {
        router.back();
    }

    const handleStartMatch = async () => {
        try {
            const response = await axios.post(`http://localhost:8080/tournament/match/${id}/start`);
            console.log(response)
            if (response.status !== 200) {
                throw new Error("Failed to start match");
            }

            console.log(response.data.message);

        } catch (error) {
            console.error("Error starting match:", error);
            toast({
                title: "Error",
                description: "Failed to start match.",
                status: "error",
                duration: 3000,
                isClosable: true,
            });
        }
        router.push('/manage-tournament');
    }

    const sendInvite = async () => {
        
        try {
            const response = await axios.post(`http://localhost:8080/admin/tournament/closed/${id}/invitation`,
                {
                    "usernames": inviteUsername.split(',')
                }
            );

            if (response.status !== 200) {
                throw new Error("Failed to invite players");
            }

            console.log(response.data.message);

        } catch (error) {
            console.error("Error starting match:", error);
            toast({
                title: "Error",
                description: "Failed to start match.",
                status: "error",
                duration: 3000,
                isClosable: true,
            });
        }
    }

    useEffect(() => {

        const tournaments = JSON.parse(sessionStorage.getItem("tournaments"));

        for (var t of tournaments) {
            if (t.id == id) {
                setTournament(t);
            }
        }

        const fetchTeams = async () => {

            try {
                const response = await axios.get(`http://localhost:8080/admin/tournament/${id}/team?page=${page}&limit=${pageSize}`);
                var test_response = test_data
                if (response.status !== 200) {
                    throw new Error("Failed to fetch teams");
                }

                const data = test_response.data;
                setTeams(data.teams);
                setCount(data.count);
            } catch (error) {
                console.error("Error fetching Teams:", error);
                toast({
                    title: "Error",
                    description: "Failed to load teams.",
                    status: "error",
                    duration: 3000,
                    isClosable: true,
                });
            }
        };

        fetchTeams();
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
                    {tournament && (
                        <Flex
                            direction={'column'}
                            margin={'auto'}
                            left={'50%'}
                            transform={"translateX(-20%)"}>
                            <Text fontSize={'2xl'} margin={'auto'} fontWeight={'bold'}>
                                {tournament.name}
                            </Text>
                            <Text fontSize={'xl'} margin={'auto'}>
                                {formatDate(convertToSGT(tournament.estimatedTournamentPeriod.tournamentBegin))} - {formatDate(convertToSGT(tournament.estimatedTournamentPeriod.tournamentEnd))}
                            </Text>
                        </Flex>
                    )}
                </Flex>
            </Box>

            <Flex
                justifyContent={'space-between'}
                m={'0% 3%'}>
                <Box
                    width={'45vw'}>
                    <TournamentForm tournament={tournament} isEdited={true} />
                </Box>

                <Flex
                    direction={'column'}
                    justifyContent={'flex-end'}>
                    <Box
                        backgroundColor={'lightgrey'}
                        borderRadius={'2%'}
                        width={'45vw'}
                        height={'120vh'}
                        position={'relative'}>
                        <Flex
                            justifyContent={'space-between'}
                            alignContent={'center'}>
                            <Text fontSize={'2xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'}>Players Registered</Text>
                        </Flex>
                        <Flex hidden={tournament?.["@type"] != "closed"} bgColor={'white'} m={'3% 2%'}>
                            <Input type="text"
                                placeholder="Type username of players to be invited"
                                value={inviteUsername}
                                onChange={handleInputChange} />
                            <Button
                                color={'white'}
                                bgColor={'deepskyblue'}
                                onClick={sendInvite}>
                                Invite
                            </Button>
                        </Flex>
                        <Flex
                            justifyContent={'space-between'}>
                            <Text fontSize={'xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'}>Username</Text>
                            <Text fontSize={'xl'} ml={'3%'} mt={'3%'} fontWeight={'bold'} mr={'3%'}>Actions</Text>
                        </Flex>
                        {teams.map((team) =>
                            <Box><ListComponent key={tournament.id} details={team} registrationEnd={tournament.registrationPeriod.registrationEnd} /></Box>
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
                    {tournament && (
                        <Button
                            margin={'1% 0% 2% 2%'}
                            backgroundColor={'mediumseagreen'}
                            color={'white'}
                            disabled={date < tournament.registrationPeriod.registrationEnd}
                            onClick={handleStartMatch}>
                            Start Tournament
                        </Button>
                    )}
                </Flex>

            </Flex>
        </>
    )

}

export default ManageTeamPage;