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
    RadioGroup,
    Grid,
} from "@chakra-ui/react";

const convertToUTCString = (datetimeStr) => {
    const localDate = new Date(datetimeStr);
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

    return `${datePart}T${timePart}`;
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
            minElo: null,
            maxElo: null,
            maxTeamCapacity: 2,
            registrationBegin: "",
            registrationEnd: "",
            tournamentBegin: "",
            tournamentEnd: "",
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
                registrationBegin: convertToSGT(tournament.registrationPeriod.registrationBegin),
                registrationEnd: convertToSGT(tournament.registrationPeriod.registrationEnd),
                tournamentBegin: convertToSGT(tournament.estimatedTournamentPeriod.tournamentBegin),
                tournamentEnd: convertToSGT(tournament.estimatedTournamentPeriod.tournamentEnd)
            });
        }
    }, [tournament, reset]);

    const validateDates = () => {
        const registrationBegin = convertToUTCString(watch("registrationBegin"));
        const registrationEnd = convertToUTCString(watch("registrationEnd"));
        const tournamentBegin = convertToUTCString(watch("tournamentBegin"));
        const tournamentEnd = convertToUTCString(watch("tournamentEnd"));

        if (registrationEnd < registrationBegin) {
            return "Registration end date/time must be later than the start date/time.";
        }

        if (tournamentBegin < registrationEnd) {
            return "Tournament start date/time must be later than the registration end date/time.";
        }

        if (tournamentEnd < tournamentBegin) {
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
            name: data.name,
            description: data.description,
            maxTeamCapacity: data.maxTeamCapacity,
            registrationPeriod: {
                registrationBegin: convertToUTCString(data.registrationBegin),
                registrationEnd: convertToUTCString(data.registrationEnd),
            },
            estimatedTournamentPeriod: {
                tournamentBegin: convertToUTCString(data.tournamentBegin),
                tournamentEnd: convertToUTCString(data.tournamentEnd),
            }
        };

        if (!isEdited) {
            requestBody["@type"] = data.tournamentType;
            requestBody.eloLimit = {
                minElo: data.minElo,
                maxElo: data.maxElo,
            };
            requestBody.createdAt = date;
            requestBody.adminUsername = data.adminUsername;
            requestBody.invited_players = data.invited_players || [];
        }

        try {
            console.log(requestBody)
            const response = isEdited ? await axios.patch(`http://localhost:8080/admin/tournament/${tournament?.id}`, requestBody) : await axios.post("http://localhost:8080/admin/tournament", requestBody);

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
            <fieldset disabled={new Date().toISOString() > tournament?.registrationPeriod?.registrationEnd}>
                <Box maxW="1000px" mx="auto" py="6" px="4">
                    {/* Tournament Details Card */}
                    <Box bg="white" p="6" boxShadow="md" borderRadius="md" mb="4">
                        <FormControl isDisabled={isEdited}>
                            <FormLabel>Tournament Type</FormLabel>
                            <RadioGroup value={tournament?.['@type']}>
                                <HStack spacing="24px">
                                    <Radio value="open" {...register("tournamentType")}>Open</Radio>
                                    <Radio value="closed" {...register("tournamentType")}>Closed</Radio>
                                </HStack>
                            </RadioGroup>
                            <Text color="red.500">{errors.tournamentType && errors.tournamentType.message}</Text>
                        </FormControl>

                        <FormControl isInvalid={errors.name} mt="4">
                            <FormLabel>Tournament Name</FormLabel>
                            <Input {...register("name", { required: "Name is required" })} />
                            <Text color="red.500">{errors.name && errors.name.message}</Text>
                        </FormControl>

                        <FormControl mt="4">
                            <FormLabel>Description</FormLabel>
                            <Input {...register("description")} />
                        </FormControl>
                    </Box>

                    {/* Player Eligibility Card */}
                    <Box bg="white" p="6" boxShadow="md" borderRadius="md" mb="4">
                        <Grid templateColumns="repeat(2, 1fr)" gap="4">
                            <FormControl isDisabled={isEdited}>
                                <FormLabel>Min. Rating</FormLabel>
                                <Input type="number" {...register("minElo", { valueAsNumber: true })} />
                            </FormControl>

                            <FormControl isDisabled={isEdited}>
                                <FormLabel>Max. Rating</FormLabel>
                                <Input type="number" {...register("maxElo", { valueAsNumber: true })} />
                            </FormControl>
                        </Grid>

                        <FormControl mt="4">
                            <FormLabel>Max. Capacity</FormLabel>
                            <Input type="number" {...register("maxTeamCapacity", { valueAsNumber: true, required: "Capacity is required" })} />
                        </FormControl>
                    </Box>

                    {/* Date Settings Card */}
                    <Box bg="white" p="6" boxShadow="md" borderRadius="md" mb="4">
                        <FormControl>
                            <FormLabel>Registration Start Date and Time</FormLabel>
                            <HStack>
                                <Input type="datetime-local" {...register("registrationBegin", { required: "Registration Start Datetime is required" })} />
                            </HStack>
                        </FormControl>

                        <FormControl isInvalid={errors.registrationEnd} mt="4">
                            <FormLabel>Registration End Date and Time</FormLabel>
                            <HStack>
                                <Input type="datetime-local" {...register("registrationEnd", { required: "Registration End Datetime is required" })} />
                                <Text color="red.500">{errors.registrationEnd && errors.registrationEnd.message}</Text>
                            </HStack>
                        </FormControl>

                        <FormControl isInvalid={errors.tournamentStart} mt="4">
                            <FormLabel>Tournament Start Date and Time</FormLabel>
                            <HStack>
                                <Input type="datetime-local" {...register("tournamentBegin", { required: "Tournament Start Datetime is required" })} />
                                <Text color="red.500">{errors.tournamentStart && errors.tournamentStart.message}</Text>
                            </HStack>
                        </FormControl>

                        <FormControl isInvalid={errors.tournamentEnd} mt="4">
                            <FormLabel>Tournament End Date and Time</FormLabel>
                            <HStack>
                                <Input type="datetime-local" {...register("tournamentEnd", { required: "Tournament End Datetime is required" })} />
                                <Text color="red.500">{errors.tournamentEnd && errors.tournamentEnd.message}</Text>
                            </HStack>
                        </FormControl>
                    </Box>

                    <Flex justifyContent="space-between" mt="4">
                        <Button backgroundColor="lightgrey" onClick={handleBackNavigation}>
                            <Text>Cancel</Text>
                        </Button>
                        <Button backgroundColor="lightblue" type="submit">
                            <Text>{isEdited ? "Save" : "Create"}</Text>
                        </Button>
                    </Flex>
                </Box>
            </fieldset>
        </form>
    );
};

export default TournamentForm;