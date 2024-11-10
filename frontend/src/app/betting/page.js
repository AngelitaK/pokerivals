"use client"
import { useEffect, useState } from "react";
import { Box, Tabs, TabList, TabPanels, Tab, TabPanel, useToast } from "@chakra-ui/react";
import axios from "../../../config/axiosInstance";
import BettingComponent from "@/components/bettingComponent";
import BetModal from "@/components/bettingModal";

const BettingPage = () => {
    const [isBetModalOpen, setBetModalOpen] = useState(false);
    const [selectedMatch, setSelectedMatch] = useState(null);
    const [betResults, setBetResults] = useState(null);
    const toast = useToast()

    useEffect(() => {
        const fetchBetResults = async () => {
            try {
                const response = await axios.get(
                    `/transaction/betting/win?page=${page}&limit=${pageSize}`
                );

                if (response.status !== 200) {
                    throw new Error("Failed to fetch bet results");
                }

                const data = response.data;
                setBetResults(data.transactions);
                setCount(data.count);
            } catch (error) {
                console.error("Error fetching bet results:", error);
                toast({
                    title: "Error",
                    description: "Failed to load bet results.",
                    status: "error",
                    duration: 3000,
                    isClosable: true,
                });
            }
        };

        fetchBetResults();
    })

    const matches = [
        { player1: "Skiddadle", player2: "Skudaddle", odds1: 0.73, odds2: 0.27, bettingPool: 1500, betStatus: "Bet" },
        { player1: "HawkTuahGurl", player2: "Bababoi", odds1: 0.49, odds2: 0.51, bettingPool: 2000, betStatus: "Betted" },
        { player1: "Player 1", player2: "Player 2", odds1: 0.31, odds2: 0.69, bettingPool: 3450, betStatus: "Bet" },
        { player1: "SuperIdol", player2: "DeXiaoRong", odds1: 0.73, odds2: 0.27, bettingPool: 2500, betStatus: "Betted" },
    ];

    const openBetModal = (match) => {
        setSelectedMatch(match);
        setBetModalOpen(true);
    };

    const closeBetModal = () => {
        setBetModalOpen(false);
        setSelectedMatch(null);
    };

    return (
        <Box p={8} minH="100vh" backgroundImage="url('/PokeRivalsBackgroundBetting.jpg')" backgroundSize="cover" backgroundPosition="center" bgOpacity="0.8">
            <Box bg="whiteAlpha.800" borderRadius="md" p={4}>
                <Tabs variant="soft-rounded" colorScheme="blue">
                    <TabList justifyContent="center" mb={2}>
                        <Tab fontWeight="bold" fontSize="lg" _selected={{ bg: "blue.600", color: "white" }}>Ongoing Matches</Tab>
                        <Tab fontWeight="bold" fontSize="lg" _selected={{ bg: "blue.600", color: "white" }}>Betting Results</Tab>
                    </TabList>
                    <TabPanels>
                        <TabPanel>
                            {matches.map((match, index) => (
                                <BettingComponent key={index} {...match} onClick={() => openBetModal(match)} />
                            ))}
                        </TabPanel>
                        {/* <TabPanel>
                            {betResults.map((result, index) => (
                                <BettingComponent
                                    key={index}
                                    player1={result.player1}
                                    player2={result.player2}
                                    odds1={result.odds1}
                                    odds2={result.odds2}
                                    bettingPool={result.bettingPool}
                                    payout={result.payout}
                                    yourBet={result.yourBet}
                                    isResult={true}
                                />
                            ))}
                        </TabPanel> */}
                    </TabPanels>
                </Tabs>
            </Box>

            {selectedMatch && (
                <BetModal
                    isOpen={isBetModalOpen}
                    onClose={closeBetModal}
                    player1={selectedMatch.player1}
                    player2={selectedMatch.player2}
                    odds1={selectedMatch.odds1}
                    odds2={selectedMatch.odds2}
                />
            )}
        </Box>
    );
};

export default BettingPage;
