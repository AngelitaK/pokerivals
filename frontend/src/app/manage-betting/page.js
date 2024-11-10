"use client";
import { useEffect, useState } from 'react';
import {
    Heading,
    Box,
    Button,
    Flex,
    FormControl,
    FormLabel,
    Input,
    Tab,
    TabList,
    TabPanel,
    TabPanels,
    Tabs,
    Text,
    VStack,
} from '@chakra-ui/react';
import { FaArrowCircleLeft, FaInfo } from "react-icons/fa";
import { useRouter } from 'next/navigation';
import axios from '../../../config/axiosInstance';
import MatchInfoModal from '@/components/matchInfoModal';

const limit = 5;

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

// ListComponent to render individual bets
const ListComponent = ({ data, type, maxProfitMargin = null, minProfitMargin = null }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);

    const handleOpenModal = () => setIsModalOpen(true);
    const handleCloseModal = () => setIsModalOpen(false);

    const teamA_wr = data.match.teams[0].winRate;
    const teamB_wr = data.match.teams[1].winRate;
    const gradient = maxProfitMargin - minProfitMargin;
    const y_intercept = minProfitMargin;
    const expected_profit = (gradient * (teamA_wr + teamB_wr) + 2 * y_intercept) / 2;
    const expected_payout = (expected_profit * (data.totalBetsOnTeamAInCents + data.totalBetsOnTeamBInCents)) / 100;

    const date = data.match.timeMatchOccurs;
    const teamABets = data.totalBetsOnTeamAInCents;
    const teamBBets = data.totalBetsOnTeamBInCents;
    const payout = type === "past" ? data.totalPaidOut / 100 : expected_payout;

    return (
        <Flex justify="space-between" py="4" borderBottom="1px solid #e2e8f0">
            <Box flex="1">
                <Text fontSize="sm">{formatDate(date)}</Text>
            </Box>
            <Text flex="1" textAlign="center">{teamABets}</Text>
            <Text flex="1" textAlign="center">{teamBBets}</Text>
            <Text flex="1" textAlign="center">{payout}</Text>
            <FaInfo onClick={handleOpenModal} cursor="pointer" />

            <MatchInfoModal isOpen={isModalOpen} onClose={handleCloseModal} matchData={data} />
        </Flex>
    );
};

const ManageBetting = () => {
    const router = useRouter();

    const [isEditingMin, setIsEditingMin] = useState(false);
    const [isEditingMax, setIsEditingMax] = useState(false);
    const [minProfitMargin, setMinProfitMargin] = useState(0);
    const [maxProfitMargin, setMaxProfitMargin] = useState(0);
    const [originalMinProfitMargin, setOriginalMinProfitMargin] = useState(0);
    const [originalMaxProfitMargin, setOriginalMaxProfitMargin] = useState(0);

    // Independent pagination states for each tab panel
    const [pastPage, setPastPage] = useState(0);
    const [futurePage, setFuturePage] = useState(0);
    const [pastBets, setPastBets] = useState([]);
    const [futureBets, setFutureBets] = useState([]);
    const [pastPageCount, setPastPageCount] = useState(0);
    const [futurePageCount, setFuturePageCount] = useState(0);

    const handleBackNavigation = () => {
        router.push("/admin-home");
    };

    const handleEditClick = (field) => {
        if (field === 'min') {
            setIsEditingMin(true);
            setOriginalMinProfitMargin(minProfitMargin);
        } else {
            setIsEditingMax(true);
            setOriginalMaxProfitMargin(maxProfitMargin);
        }
    };

    const handleSaveClick = async (field) => {
        try {
            const response = await axios.patch('/transaction/admin/configuration', {
                name: field === 'min' ? "MINIMUM_PROFIT_MARGIN_PERCENTAGE" : "MAXIMUM_PROFIT_MARGIN_PERCENTAGE",
                amount: field === 'min' ? parseFloat(minProfitMargin) : parseFloat(maxProfitMargin),
            });

            if (response.status !== 200) {
                throw new Error("Failed to update data");
            }
            field === 'min' ? setIsEditingMin(false) : setIsEditingMax(false);
        } catch (error) {
            console.error("Error updating data: ", error);
        }
    };

    const handleCancelClick = (field) => {
        if (field === 'min') {
            setIsEditingMin(false);
            setMinProfitMargin(originalMinProfitMargin);
        } else {
            setIsEditingMax(false);
            setMaxProfitMargin(originalMaxProfitMargin);
        }
    };

    const fetchBets = async (type, page) => {
        try {
            const endpoint = type === 'past' ? '/transaction/admin/past-bets' : '/transaction/admin/future-bets';
            const response = await axios.get(endpoint, { params: { page, limit } });

            if (response.status !== 200) {
                throw new Error(`Failed to fetch ${type} bets`);
            }

            const data = response.data

            if (type === 'past') {
                setPastBets(data.summaries);
                setPastPageCount(data.count);
            } else {
                setFutureBets(data.summaries);
                setFuturePageCount(data.count);
            }

        } catch (error) {
            console.error(`Error fetching ${type} bets: `, error);
        }
    };

    // Load initial configuration and past bets when component mounts
    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await axios.get('/transaction/admin/configuration');

                if (response.status === 200) {
                    const data = response.data;
                    setMinProfitMargin(data[0].amount);
                    setMaxProfitMargin(data[1].amount);
                } else {
                    throw new Error("Failed to fetch configuration data");
                }
            } catch (error) {
                console.error("Error fetching configuration data: ", error);
            }
        };

        fetchData();
    }, []);

    // Fetch future bets whenever the futurePage changes
    useEffect(() => {
        fetchBets('past', pastPage);
    }, [pastPage]);

    // Fetch future bets whenever the futurePage changes
    useEffect(() => {
        fetchBets('future', futurePage);
    }, [futurePage]);

    const totalPastPages = Math.ceil(pastPageCount / limit);
    const totalFuturePages = Math.ceil(futurePageCount / limit);

    return (
        <Box p="4" backgroundColor="gray.100" minH="100vh">
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

            <Flex direction={['column', 'row']} p={6} gap={10} align="flex-start">
                {/* Left side form with balanced height */}
                <Box width={['100%', '17%']} bg="gray.50" p={6} borderRadius="md" boxShadow="md">
                    <VStack spacing={4} align="stretch">
                        <Text fontSize="lg" fontWeight="bold" mb={4}>Betting Settings</Text>

                        {/* Min. Profit Margin % */}
                        <FormControl>
                            <FormLabel>Min. Profit Margin %</FormLabel>
                            <Flex align="center">
                                <Input
                                    type="number"
                                    value={minProfitMargin}
                                    onChange={(e) => setMinProfitMargin(e.target.value)}
                                    isReadOnly={!isEditingMin}
                                    bg={!isEditingMin ? 'gray.200' : 'white'}
                                />
                                {isEditingMin ? (
                                    <>
                                        <Button colorScheme="blue" ml={2} onClick={() => handleSaveClick('min')}>
                                            Save
                                        </Button>
                                        <Button colorScheme="red" ml={2} onClick={() => handleCancelClick('min')}>
                                            Cancel
                                        </Button>
                                    </>
                                ) : (
                                    <Button colorScheme="teal" ml={2} onClick={() => handleEditClick('min')}>
                                        Edit
                                    </Button>
                                )}
                            </Flex>
                        </FormControl>

                        {/* Max. Profit Margin % */}
                        <FormControl>
                            <FormLabel>Max. Profit Margin %</FormLabel>
                            <Flex align="center">
                                <Input
                                    type="number"
                                    value={maxProfitMargin}
                                    onChange={(e) => setMaxProfitMargin(e.target.value)}
                                    isReadOnly={!isEditingMax}
                                    bg={!isEditingMax ? 'gray.200' : 'white'}
                                />
                                {isEditingMax ? (
                                    <>
                                        <Button colorScheme="blue" ml={2} onClick={() => handleSaveClick('max')}>
                                            Save
                                        </Button>
                                        <Button colorScheme="red" ml={2} onClick={() => handleCancelClick('max')}>
                                            Cancel
                                        </Button>
                                    </>
                                ) : (
                                    <Button colorScheme="teal" ml={2} onClick={() => handleEditClick('max')}>
                                        Edit
                                    </Button>
                                )}
                            </Flex>
                        </FormControl>
                    </VStack>
                </Box>

                {/* Right side tabs for bets */}
                <Box flex="1" bg="gray.50" p={4} borderRadius="md" boxShadow="md" display="flex" flexDirection="column" justifyContent="space-between">
                    <Tabs variant="soft-rounded" colorScheme="teal" flex="1">
                        <TabList>
                            <Tab>Past Bets</Tab>
                            <Tab>Future Bets</Tab>
                        </TabList>
                        <TabPanels>
                            <TabPanel>
                                <Box>
                                    <Flex justify="space-between" py="2" borderBottom="1px solid #e2e8f0">
                                        <Text flex="1" fontWeight="bold">Details</Text>
                                        <Text flex="1" textAlign="center" fontWeight="bold">Team A Bets</Text>
                                        <Text flex="1" textAlign="center" fontWeight="bold">Team B Bets</Text>
                                        <Text flex="1" textAlign="center" fontWeight="bold">Total Payout</Text>
                                    </Flex>
                                    {pastBets && pastBets.map((bet, i) => (
                                        <ListComponent
                                            key={i}
                                            data={bet}
                                            type={'past'}
                                        />
                                    ))}
                                </Box>
                                <Flex justify="center" mt={4} mb={2}>
                                    <Button onClick={() => setPastPage(prev => Math.max(prev - 1, 0))} isDisabled={pastPage === 0} mx={2}>Previous</Button>
                                    <Text fontSize="sm" alignSelf="center">{`Page ${totalPastPages === 0 ? 0 : pastPage + 1} of ${totalPastPages}`}</Text>
                                    <Button onClick={() => setPastPage((prev) => Math.min(prev + 1, totalPastPages - 1))} isDisabled={pastPage === totalPastPages - 1} mx={2}>Next</Button>
                                </Flex>
                            </TabPanel>
                            <TabPanel>
                                <Box>
                                    <Flex justify="space-between" py="2" borderBottom="1px solid #e2e8f0">
                                        <Text flex="1" fontWeight="bold">Details</Text>
                                        <Text flex="1" textAlign="center" fontWeight="bold">Team A Bets</Text>
                                        <Text flex="1" textAlign="center" fontWeight="bold">Team B Bets</Text>
                                        <Text flex="1" textAlign="center" fontWeight="bold">Estimated Profit</Text>
                                    </Flex>
                                    {futureBets && futureBets.map((bet, i) => (
                                        <ListComponent
                                            key={i}
                                            data={bet}
                                            type={'future'}
                                            minProfitMargin={minProfitMargin}
                                            maxProfitMargin={maxProfitMargin}
                                        />
                                    ))}
                                </Box>
                                <Flex justify="center" mt={4} mb={2}>
                                    <Button onClick={() => setFuturePage(prev => Math.max(prev - 1, 0))} isDisabled={futurePage === 0} mx={2}>Previous</Button>
                                    <Text fontSize="sm" alignSelf="center">{`Page ${totalFuturePages === 0 ? 0 : futurePage + 1} of ${totalFuturePages}`}</Text>
                                    <Button onClick={() => setFuturePage((prev) => Math.min(prev + 1, totalFuturePages - 1))} isDisabled={futurePage === totalFuturePages - 1} mx={2}>Next</Button>
                                </Flex>
                            </TabPanel>
                        </TabPanels>
                    </Tabs>
                </Box>
            </Flex>
        </Box>
    );
};

export default ManageBetting;
