"use client"
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "../../config/axiosInstance";
import { useForm } from "react-hook-form";
import {
    Box,
    Flex,
    Text,
    Button,
    Input,
    Checkbox,
    HStack,
    FormControl,
    FormLabel,
    Radio,
    RadioGroup
} from "@chakra-ui/react";

const convertToUTCString = (dateStr, timeStr) => {
    const localDate = new Date(`${dateStr}T${timeStr}:00`);
    return localDate.toISOString(); // Convert Singapore time to UTC and format as ISO string
};

const convertToSGT = (utcDateStr) => {

    const utcDate = new Date(utcDateStr); // Create Date from UTC string
    const datePart = utcDate.toLocaleDateString("en-CA", { timeZone: "Asia/Singapore" }); // "YYYY-MM-DD" format
    const timePart = utcDate.toLocaleTimeString("en-GB", {
        timeZone: "Asia/Singapore",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
    }); // "HH:MM" 24-hour format

    return { datePart, timePart };
};

var date = new Date().toISOString();

const TournamentForm = ({ tournament = null, isEdited = false }) => {

    const router = useRouter();

    const {
        register,
        handleSubmit,
        formState: { errors },
        watch,
        reset
    } = useForm({
        defaultValues:
        {
            tournamentType: "open",
            name: "",
            description: "",
            minElo: 0,
            maxElo: 0,
            maxTeamCapacity: 2,
            registrationDateBegin: "",
            registrationTimeBegin: "",
            registrationDateEnd: "",
            registrationTimeEnd: "",
            tournamentDateBegin: "",
            tournamentTimeBegin: "",
            tournamentDateEnd: "",
            tournamentTimeEnd: "",
        }
    });

    useEffect(() => {
        if (tournament) {
            reset({
                tournamentType: tournament['@type'],
                name: tournament.name,
                description: tournament.description,
                minElo: tournament.eloLimit.minElo,
                maxElo: tournament.eloLimit.maxElo,
                maxTeamCapacity: tournament.maxTeamCapacity,
                registrationDateBegin: convertToSGT(tournament.registrationPeriod.registrationBegin).datePart,
                registrationTimeBegin: convertToSGT(tournament.registrationPeriod.registrationBegin).timePart,
                registrationDateEnd: convertToSGT(tournament.registrationPeriod.registrationEnd).datePart,
                registrationTimeEnd: convertToSGT(tournament.registrationPeriod.registrationEnd).timePart,
                tournamentDateBegin: convertToSGT(tournament.estimatedTournamentPeriod.tournamentBegin).datePart,
                tournamentTimeBegin: convertToSGT(tournament.estimatedTournamentPeriod.tournamentBegin).timePart,
                tournamentDateEnd: convertToSGT(tournament.estimatedTournamentPeriod.tournamentEnd).datePart,
                tournamentTimeEnd: convertToSGT(tournament.estimatedTournamentPeriod.tournamentEnd).timePart
            });
        }
    }, [tournament, reset]);

    const validateDates = () => {
        const registrationStartDate = convertToUTCString(watch("registrationDateBegin"), watch("registrationTimeBegin"));
        const registrationEndDate = convertToUTCString(watch("registrationDateEnd"), watch("registrationTimeEnd"));
        const tournamentStartDate = convertToUTCString(watch("tournamentDateBegin"), watch("tournamentTimeBegin"));
        const tournamentEndDate = convertToUTCString(watch("tournamentDateEnd"), watch("tournamentTimeEnd"));

        if (registrationEndDate < registrationStartDate) {
            return "Registration end date/time must be later than the start date/time.";
        }

        if (tournamentStartDate < registrationEndDate) {
            return "Tournament start date/time must be later than the registration end date/time.";
        }

        if (tournamentEndDate < tournamentStartDate) {
            return "Tournament end date/time must be later than the start date/time.";
        }

        return true; // Valid
    };

    const validateElo = () => {
        if (watch("maxElo") < watch("minElo")) {
            return "Max. rating must be higher than the min. rating.";
        }

        return true;
    }

    const onSubmit = async (data) => {
        const requestBody = {
            "@type": data.tournamentType,
            name: data.name,
            description: data.description,
            maxTeamCapacity: data.maxTeamCapacity,
            eloLimit: {
                minElo: data.minElo,
                maxElo: data.maxElo,
            },
            registrationPeriod: {
                registrationBegin: convertToUTCString(data.registrationDateBegin, data.registrationTimeBegin),
                registrationEnd: convertToUTCString(data.registrationDateEnd, data.registrationTimeEnd),
            },
            estimatedTournamentPeriod: {
                tournamentBegin: convertToUTCString(data.tournamentDateBegin, data.tournamentTimeBegin),
                tournamentEnd: convertToUTCString(data.tournamentDateEnd, data.tournamentTimeEnd),
            },
            createdAt: date,
        };

        try {
            console.log(requestBody)
            if (isEdited) {
                var response = await axios.patch(
                    `http://localhost:8080/admin/tournament/${tournament?.id}`,
                    requestBody
                );
            } else {
                var response = await axios.post(
                    `http://localhost:8080/admin/tournament`,
                    requestBody
                );
            }

            if (response.status !== 200) {
                throw new Error(`Failed to ${isEdited ? "update" : "create"} tournament`);
            }

            console.log(`Tournament ${isEdited ? "updated" : "created"}:`, response.data);

            router.push("/manage-tournament");
        } catch (error) {
            console.error("Error creating tournament:", error);
        }
    };

    const handleBackNavigation = () => {
        router.back();
    };

    return (
        <form onSubmit={handleSubmit((data) => {
            const dateValidation = validateDates();
            const eloValidation = validateElo();
            if (dateValidation === true && eloValidation === true) {
                onSubmit(data);
            } else {
                if (dateValidation !== true) { alert(dateValidation) };
                if (eloValidation !== true) { alert(eloValidation) };
            }
        })}>

            <Flex mb={"3%"} height={"120vh"} flexDirection={"column"} justifyContent={"space-between"}>
                <FormControl isDisabled={isEdited}>
                    <FormLabel>Tournament Type</FormLabel>
                    <RadioGroup value={tournament?.['@type'] || "open"}>
                        <HStack spacing="24px">
                            <Radio value="open" {...register("tournamentType")}>
                                Open
                            </Radio>
                            <Radio value="closed" {...register("tournamentType")}>
                                Closed
                            </Radio>
                        </HStack>
                    </RadioGroup>
                    <Text color="red.500">{errors.tournamentType && errors.tournamentType.message}</Text>
                </FormControl>

                <FormControl isInvalid={errors.name}>
                    <FormLabel>Tournament Name</FormLabel>
                    <Input {...register("name", { required: "Name is required" })} />
                    <Text color="red.500">{errors.name && errors.name.message}</Text>
                </FormControl>

                <FormControl>
                    <FormLabel>Description</FormLabel>
                    <Input {...register("description")} />
                </FormControl>

                <FormControl>
                    <FormLabel>Max. Capacity</FormLabel>
                    <Input type="number" {...register("maxTeamCapacity", { valueAsNumber: true, required: "Capacity is required" })} />
                </FormControl>

                <FormControl isDisabled={isEdited}>
                    <FormLabel>Min. Rating</FormLabel>
                    <Input type="number" {...register("minElo", { valueAsNumber: true })} />
                </FormControl>

                <FormControl isDisabled={isEdited}>
                    <FormLabel>Max. Rating</FormLabel>
                    <Input type="number" {...register("maxElo", { valueAsNumber: true })} />
                </FormControl>

                <FormControl isDisabled={date > tournament?.registrationPeriod.registrationEnd}>
                    <FormLabel>Registration Start Date and Time</FormLabel>
                    <HStack>
                        <Input type="date" {...register("registrationDateBegin", { required: "Registration Start Date is required" })} />
                        <Input type="time" {...register("registrationTimeBegin", { required: "Registration Start Time is required" })} />
                    </HStack>
                </FormControl>

                <FormControl isInvalid={errors.registrationEnd} isDisabled={date > tournament?.registrationPeriod.registrationEnd}>
                    <FormLabel>Registration End Date and Time</FormLabel>
                    <HStack>
                        <Input type="date" {...register("registrationDateEnd", { required: "Registration End Date is required" })} />
                        <Input type="time" {...register("registrationTimeEnd", { required: "Registration End Time is required" })} />
                        <Text color="red.500">{errors.registrationEnd && errors.registrationEnd.message}</Text>
                    </HStack>
                </FormControl>

                <FormControl isInvalid={errors.tournamentStart}>
                    <FormLabel>Tournament Start Date and Time</FormLabel>
                    <HStack>
                        <Input type="date" {...register("tournamentDateBegin", { required: "Tournament Start Date is required" })} />
                        <Input type="time" {...register("tournamentTimeBegin", { required: "Tournament Start Time is required" })} />
                        <Text color="red.500">{errors.tournamentStart && errors.tournamentStart.message}</Text>
                    </HStack>
                </FormControl>

                <FormControl isInvalid={errors.tournamentEnd}>
                    <FormLabel>Tournament End Date and Time</FormLabel>
                    <HStack>
                        <Input type="date" {...register("tournamentDateEnd", { required: "Tournament End Date is required" })} />
                        <Input type="time" {...register("tournamentTimeEnd", { required: "Tournament End Time is required" })} />
                        <Text color="red.500">{errors.tournamentEnd && errors.tournamentEnd.message}</Text>
                    </HStack>
                </FormControl>
            </Flex>

            <Box>
                <Flex align={"center"} margin={"1% 0% 2% 2%"}>
                    <Button backgroundColor={"lightblue"} type="submit">
                        <Text>{isEdited ? "Save" : "Create"}</Text>
                    </Button>
                    <Button backgroundColor={"lightgrey"} onClick={handleBackNavigation} ml={"1%"}>
                        <Text>Cancel</Text>
                    </Button>
                </Flex>
            </Box>
        </form>
    );
};

export default TournamentForm;