"use client";
import React, { useState, useEffect } from "react";
import Calendar from "../../components/calendar";
import axios from "../../../config/axiosInstance";
import { FaArrowCircleLeft } from "react-icons/fa";
import { Box, Flex, Text } from "@chakra-ui/react";
import { useRouter } from "next/navigation";
// import test_data from "./test_data";

const formatDate = (isoString) => {
    const date = new Date(isoString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0"); // Months are 0-based
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const CalendarPage = () => {
    const router = useRouter();

    const handleBackNavigation = () => {
        router.back();
    };

    const [matches, setMatches] = useState([]);

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const now = new Date();
                const startDate = new Date(now.getFullYear(), now.getMonth(), 1);
                const endDate = new Date(now.getFullYear(), now.getMonth(), 0);

                const response = await axios.get(
                    `http://localhost:8080/tournament/match/me/player`,
                    {
                        params: {
                            start: startDate.toISOString(),
                            end: endDate.toISOString(),
                            page: 0,
                            limit: 100,
                        },
                    }
                );

                if (response.status !== 200) {
                    throw new Error("Failed to fetch Events");
                }
                
                const matches = response.data.matches; // test_data

                const events = matches.matches.map((match) => ({
                    id: match.tournament_id,
                    title: `Tournament ${match.tournament_id}`,
                    start: formatDate(match.timeMatchOccurs),
                    end: formatDate(match.timeMatchEnds), // Assuming the event ends on the same day
                    calendarId: match.both_agree_timing
                }));

                setMatches(events);
            } catch (error) {
                console.error("Error fetching Events:", error);
            }
        };

        fetchEvents();
    }, []);

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
                    <Calendar matches={matches} />
                </Box>
            </Flex>
        </>
    );
};

export default CalendarPage;