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
    HStack,
    FormControl,
    FormLabel,
    Radio,
    RadioGroup,
    Grid,
    FormErrorMessage,
} from "@chakra-ui/react";

const convertToUTCString = (datetimeStr) => {
    const localDate = new Date(datetimeStr);
    return localDate.toISOString(); // Convert Singapore time to UTC and format as ISO string
};

const convertToSGT = (utcDateStr) => {
    const utcDate = new Date(utcDateStr);
    const datePart = utcDate.toLocaleDateString("en-CA", { timeZone: "Asia/Singapore" });
    const timePart = utcDate.toLocaleTimeString("en-GB", {
        timeZone: "Asia/Singapore",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
    });
    return `${datePart}T${timePart}`;
};

const TournamentForm = ({ tournament = null, isEdited = false }) => {
    const router = useRouter();

    const {
        register,
        handleSubmit,
        formState: { errors },
        setError,
        clearErrors,
        reset,
        watch,
    } = useForm({
        defaultValues: {
            tournamentType: "open",
            name: "",
            description: "",
            minElo: 0,
            maxElo: 1000,
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

        let valid = true;
        clearErrors("registrationEnd");
        clearErrors("tournamentBegin");
        clearErrors("tournamentEnd");

        if (registrationEnd < registrationBegin) {
            setError("registrationEnd", { type: "manual", message: "Registration end date/time must be later than the start date/time." });
            valid = false;
        }

        if (tournamentBegin < registrationEnd) {
            setError("tournamentBegin", { type: "manual", message: "Tournament start date/time must be later than the registration end date/time." });
            valid = false;
        }

        if (tournamentEnd < tournamentBegin) {
            setError("tournamentEnd", { type: "manual", message: "Tournament end date/time must be later than the start date/time." });
            valid = false;
        }

        return valid;
    };

    const validateElo = () => {
        clearErrors("maxElo");

        if (watch("maxElo") < watch("minElo")) {
            setError("maxElo", { type: "manual", message: "Max. rating must be higher than the min. rating." });
            return false;
        }

        return true;
    }

    const onSubmit = async (data) => {
        if (!validateDates() || !validateElo()) return;

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
        }

        try {
            const response = isEdited 
                ? await axios.patch(`http://localhost:8080/admin/tournament/${tournament?.id}`, requestBody) 
                : await axios.post("http://localhost:8080/admin/tournament", requestBody);

            if (response.status !== 200) {
                throw new Error(`Failed to ${isEdited ? "update" : "create"} tournament`);
            }

            router.push("/manage-tournament");
        } catch (error) {
            console.error("Error creating tournament:", error);
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <fieldset disabled={new Date().toISOString() > tournament?.registrationPeriod?.registrationEnd}>
                <Box maxW="1000px" mx="auto" py="6" px="4">
                    <Box bg="white" p="6" boxShadow="md" borderRadius="md" mb="4">
                        <FormControl isDisabled={isEdited} isInvalid={errors.tournamentType}>
                            <FormLabel>Tournament Type</FormLabel>
                            <RadioGroup defaultValue="open">
                                <HStack spacing="24px">
                                    <Radio value="open" {...register("tournamentType")}>Open</Radio>
                                    <Radio value="closed" {...register("tournamentType")}>Closed</Radio>
                                </HStack>
                            </RadioGroup>
                            <FormErrorMessage>{errors.tournamentType && errors.tournamentType.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={errors.name} mt="4">
                            <FormLabel>Tournament Name</FormLabel>
                            <Input {...register("name", { required: "Name is required" })} />
                            <FormErrorMessage>{errors.name && errors.name.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl mt="4">
                            <FormLabel>Description</FormLabel>
                            <Input {...register("description")} />
                        </FormControl>
                    </Box>

                    <Box bg="white" p="6" boxShadow="md" borderRadius="md" mb="4">
                        <Grid templateColumns="repeat(2, 1fr)" gap="4">
                            <FormControl isDisabled={isEdited}>
                                <FormLabel>Min. Rating</FormLabel>
                                <Input type="number" {...register("minElo", { valueAsNumber: true })} />
                            </FormControl>

                            <FormControl isDisabled={isEdited} isInvalid={errors.maxElo}>
                                <FormLabel>Max. Rating</FormLabel>
                                <Input type="number" {...register("maxElo", { valueAsNumber: true })} />
                                <FormErrorMessage>{errors.maxElo && errors.maxElo.message}</FormErrorMessage>
                            </FormControl>
                        </Grid>

                        <FormControl mt="4" isInvalid={errors.maxTeamCapacity}>
                            <FormLabel>Max. Capacity</FormLabel>
                            <Input type="number" {...register("maxTeamCapacity", { valueAsNumber: true, required: "Capacity is required" })} />
                            <FormErrorMessage>{errors.maxTeamCapacity && errors.maxTeamCapacity.message}</FormErrorMessage>
                        </FormControl>
                    </Box>

                    <Box bg="white" p="6" boxShadow="md" borderRadius="md" mb="4">
                        <FormControl isInvalid={errors.registrationBegin}>
                            <FormLabel>Registration Start Date and Time</FormLabel>
                            <Input type="datetime-local" {...register("registrationBegin", { required: "Registration Start Datetime is required" })} />
                            <FormErrorMessage>{errors.registrationBegin && errors.registrationBegin.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={errors.registrationEnd} mt="4">
                            <FormLabel>Registration End Date and Time</FormLabel>
                            <Input type="datetime-local" {...register("registrationEnd", { required: "Registration End Datetime is required" })} />
                            <FormErrorMessage>{errors.registrationEnd && errors.registrationEnd.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={errors.tournamentBegin} mt="4">
                            <FormLabel>Tournament Start Date and Time</FormLabel>
                            <Input type="datetime-local" {...register("tournamentBegin", { required: "Tournament Start Datetime is required" })} />
                            <FormErrorMessage>{errors.tournamentBegin && errors.tournamentBegin.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={errors.tournamentEnd} mt="4">
                            <FormLabel>Tournament End Date and Time</FormLabel>
                            <Input type="datetime-local" {...register("tournamentEnd", { required: "Tournament End Datetime is required" })} />
                            <FormErrorMessage>{errors.tournamentEnd && errors.tournamentEnd.message}</FormErrorMessage>
                        </FormControl>
                    </Box>

                    <Flex justifyContent="space-between" mt="4">
                        <Button backgroundColor="lightgrey" onClick={() => router.back()}>
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
