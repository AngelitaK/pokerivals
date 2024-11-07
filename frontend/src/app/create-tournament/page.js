"use client"
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
    Box,
    Flex,
    Text
} from "@chakra-ui/react";
import {
    FaArrowCircleLeft
} from "react-icons/fa";
import TournamentForm from "@/components/tournamentForm";

const CreateTournamentPage = () => {
    const router = useRouter();

    const handleBackNavigation = () => {
        router.back();
    }

    return (
        <>
            <Box>
                <Flex align={"center"} margin={"1% 0% 2% 2%"}>
                    <Flex align={"center"} onClick={handleBackNavigation}>
                        <FaArrowCircleLeft size={"4vh"} />
                        <Text ml={"1vh"} fontSize={"3xl"}>
                            Back
                        </Text>
                    </Flex>

                    <Text fontSize={"3xl"} margin={"auto"} fontWeight={"bold"} transform={"translateX(-30%)"}>
                        Create Tournament
                    </Text>
                </Flex>
            </Box>
            <Flex
                justifyContent={'center'}>
                <Box
                    width={'80vw'}
                    m={'0% 3%'}>
                    <TournamentForm />
                </Box>
            </Flex>

        </>

    )
};

export default CreateTournamentPage;