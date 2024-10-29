"use client"
import React, { useState, useEffect } from 'react';
import Calendar from '../../components/calendar';
import { FaArrowCircleLeft } from 'react-icons/fa';
import { Box, Flex, Text } from '@chakra-ui/react';
import { useRouter } from 'next/navigation';

const CalendarPage = () => {
    const router = useRouter();

    const handleNavigation = () => {
        router.push("/");
    }

    const [events, setEvents] = useState([
        { id: '1', title: 'Meeting', start: '2023-11-01 10:00', end: '2023-11-01 12:00' },
        { id: '2', title: 'Conference', start: '2023-11-07', end: '2023-11-10'},
        { id: '3', title: 'Lunch with Team', start: '2023-11-14 12:00', end: '2023-11-14 13:00' },
    ]);

    useEffect(() => {
        const fetchEvents = async () => {
          try {
            const response = await fetch("http://localhost:8090/player/me/friend", {
              method: "GET",
              credentials: "include", // Maintain session
              headers: {
                "Content-Type": "application/json",
              },
            });
            
            if (!response.ok) {
              throw new Error("Failed to fetch Events");
            }
  
            const data = await response.json();
            setEvents(data); // Set the Event list
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
                align={'center'}
                margin={'1% 0% 2% 2%'}
                onClick={handleNavigation}
                > 
                    <FaArrowCircleLeft
                    size={'4vh'}
                    />
                    <Text ml={'1vh'} fontSize={'3xl'}>Back</Text>
                    <Text fontSize={'3xl'} fontWeight={'bold'} margin={'auto'}>My Tournament Schedule</Text>
                </Flex>
                
            </Box>
            <Flex
            justifyContent={'center'}
            >
                <Box
                width={'80vw'}
                mb={'2%'}>
                    <Calendar events={events}/> 
                </Box>         
            </Flex>
        </>
    );
};

export default CalendarPage;
