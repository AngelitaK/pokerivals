"use client";
import React, { useState, useEffect, useMemo } from "react";
import Calendar from "@/components/calendar";
import axios from "../../../config/axiosInstance";
import { FaArrowCircleLeft } from "react-icons/fa";
import { Box, Flex, Text } from "@chakra-ui/react";
import { useRouter } from "next/navigation";
import useAuth from "../../../config/useAuth";
import LoadingOverlay from "../../components/loadingOverlay";

const formatDate = (isoString) => {
    const date = new Date(isoString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const CalendarPage = () => {
    const router = useRouter();
    const [matches, setMatches] = useState([]);
    const [currentDate, setCurrentDate] = useState(new Date());

    const roles = useMemo(() => ["PLAYER", "ADMIN"], []); // Memoize roles array
    const { isAuthenticated, user, loading } = useAuth(roles);

    const handleBackNavigation = () => {
        router.back();
    };

    const fetchEvents = async (month, year) => {
        
        try {
            const startDate = new Date(year, month - 1, 1);
            const endDate = new Date(year, month + 3, 0);
            const limit = 100;

            let page = 0;
            let allMatches = [];
            let totalMatches = 0;

            do {
                const response = await axios.get(
                    `/tournament/match/me/player`,
                    {
                        params: {
                            start: startDate.toISOString(),
                            end: endDate.toISOString(),
                            page: page,
                            limit: limit,
                        },
                    }
                );

                if (response.status !== 200) {
                    throw new Error("Failed to fetch Events");
                }

                const { matches, count } = response.data;
                
                const events = matches.map((match) => {
                    const startDate = new Date(match.timeMatchOccurs);
                    const endDate = new Date(startDate);
                    endDate.setMinutes(endDate.getMinutes() + 20); // Add 20 minutes
                
                    return {
                        id: match.tournament_id,
                        title: `Tournament ${match.tournamentName}`,
                        start: formatDate(startDate),
                        end: formatDate(endDate),
                        calendarId: match.both_agree_timing,
                    };
                });

                allMatches = [...allMatches, ...events];
                totalMatches = count;
                page += 1;
            } while (allMatches.length < totalMatches);

            setMatches(allMatches);
        } catch (error) {
            console.error("Error fetching Events:", error);
        }
    };


    useEffect(() => {
        fetchEvents(currentDate.getMonth(), currentDate.getFullYear());
    }, [currentDate]);

    const handleMonthChange = (date) => {
        var new_date = new Date(date)
        console.log(new_date)
        setCurrentDate(new_date);
    };

    if (loading) return <LoadingOverlay />;
    if (!isAuthenticated) return null;

    return (
        <>
            <Box>
                <Flex
                    align={"center"}
                    margin={"1% 0% 2% 2%"}
                    onClick={handleBackNavigation}
                >
                    <FaArrowCircleLeft size={"4vh"} />
                    <Text ml={"1vh"} fontSize={"3xl"}>
                        Back
                    </Text>
                    <Text fontSize={"3xl"} fontWeight={"bold"} margin={"auto"}>
                        My Tournament Schedule
                    </Text>
                </Flex>
            </Box>
            <Flex justifyContent={"center"}>
                <Box width={"80vw"} mb={"2%"}>
                    <Calendar matches={matches} onMonthChange={handleMonthChange} />
                </Box>
            </Flex>
        </>
    );
};

export default CalendarPage;