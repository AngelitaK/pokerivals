"use client"
import React, { useEffect, useState } from 'react';
import axios from '../../../../config/axiosInstance';
import { useRouter } from 'next/navigation';
import {
    Box,
    Text,
    Flex,
    Tabs,
    TabList,
    TabPanels,
    Tab,
    TabPanel,
    useToast,
    Heading
} from '@chakra-ui/react';
import {
    FaArrowCircleLeft,
} from 'react-icons/fa';
import MatchComponent from '@/components/matchComponent';
import test_data from './test-data';

const ManageMatchesPage = () => {
    const router = useRouter();
    const [tournamentData, setTournamentData] = useState([]);
    const toast = useToast();

    // Fetch tournament data on page load
    useEffect(() => {
        // axios.get('/api/tournament-data')
        //   .then(response => setTournamentData(response.data))
        //   .catch(error => console.error('Error fetching tournament data:', error));
        setTournamentData(test_data);
    }, []);

    const handleBackNavigation = () => {
        router.back();
    }

    return (
        <>
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
                {tournamentData && (
                    <Flex
                        direction={'column'}
                        margin={'auto'}
                        left={'50%'}
                        transform={"translateX(-20%)"}>
                        <Text fontSize={'2xl'} margin={'auto'} fontWeight={'bold'}>
                            Boombayah Battle {/* {tournament.name} */}
                        </Text>
                        <Text fontSize={'xl'} margin={'auto'}>
                            13 OCT 2024, 12PM - 14 OCT 2024, 1PM {/* {formatDate(convertToSGT(tournament.estimatedTournamentPeriod.tournamentBegin))} - {formatDate(convertToSGT(tournament.estimatedTournamentPeriod.tournamentEnd))} */}
                        </Text>
                    </Flex>
                )}
            </Flex>
            <Box
            m={'0% 3%'}>
                <Tabs variant="enclosed" colorScheme="teal">
                    <TabList>
                        {tournamentData.map((round, index) => (
                            <Tab key={index}>{round.title}</Tab>
                        ))}
                    </TabList>
                    <TabPanels>
                        {tournamentData.map((round, index) => (
                            <TabPanel key={index} p={4}>
                                {round.seeds.map((seed, seedIndex) => (
                                    <MatchComponent
                                        key={seedIndex}
                                        seed={seed}
                                        toast={toast}
                                    />
                                ))}
                            </TabPanel>
                        ))}
                    </TabPanels>
                </Tabs>
            </Box>

        </>
    );
}

export default ManageMatchesPage;