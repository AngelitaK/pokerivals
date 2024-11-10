"use client";

import {
  Box,
  Text,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  VStack,
  Stack,
  Icon,
  Flex,
  Heading,
  Button,
} from "@chakra-ui/react";
import LeaderBoardMember from "@/components/leaderboardMember";

const LeaderBoard = () => {
  const renderLeaderboardMembers = () => {
    return Array.from({ length: 10 }).map((_, index) => (
      <LeaderBoardMember key={index} />
    ));
  };

  return (
    <Box
      p={8}
      minH="100vh"
      backgroundImage="url('/leaderboardBG.png')"
      backgroundSize="cover"
      backgroundPosition="center"
      bgopacity="0.8"
    >
      <Heading textAlign="center" pb={6}>
        Leaderboard
      </Heading>

      <Box bg="whiteAlpha.800" borderRadius="md" boxShadow="md" p={4}>
        <Tabs variant="soft-rounded" colorScheme="blue">

          {/* tabs to change list */}
          <TabList justifyContent="center">
            <Tab
              fontWeight="bold"
              fontSize="lg"
              _selected={{ bg: "blue.600", color: "white" }}
            >
              Global Ranking
            </Tab>
            <Tab
              fontWeight="bold"
              fontSize="lg"
              _selected={{ bg: "blue.600", color: "white" }}
            >
              Team Ranking
            </Tab>
            <Tab
              fontWeight="bold"
              fontSize="lg"
              _selected={{ bg: "blue.600", color: "white" }}
            >
              Friends Ranking
            </Tab>
          </TabList>

          <TabPanels>
            {/* global ranking /player/me/non-friend */}
            <TabPanel>
                <Flex fontWeight="bold" my={3} color="black" justify="space-between">
                  <Text mx={10} fontSize="xl  ">
                    Player
                  </Text>
                  <Text mx={10} fontSize="xl">
                   Clan
                  </Text>
                  <Text mx={10} fontSize="xl">
                    Points
                  </Text>
                </Flex>

                <VStack alignItems="stretch" w="100%">
                  {renderLeaderboardMembers()}
                </VStack>
            </TabPanel>

            {/* global ranking /player/clan/{name} */}
            <TabPanel>
            <Flex fontWeight="bold" my={3} color="black" justify="space-between">
                  <Text mx={10} fontSize="xl">
                    Player
                  </Text>
                  <Text mx={10} fontSize="xl">
                   Clan
                  </Text>
                  <Text mx={10} fontSize="xl">
                    Points
                  </Text>
                </Flex>

                <VStack alignItems="stretch" w="100%">
                  {renderLeaderboardMembers()}
                </VStack>
            </TabPanel>

            {/* global ranking /player/me/friend + myself */}
            <TabPanel>
            <Flex fontWeight="bold" my={3} color="black" justify="space-between">
                  <Text mx={10} fontSize="xl">
                    Player
                  </Text>
                  <Text mx={10} fontSize="xl">
                   Clan
                  </Text>
                  <Text mx={10} fontSize="xl">
                    Points
                  </Text>
                </Flex>

                <VStack alignItems="stretch" w="100%">
                  {renderLeaderboardMembers()}
                </VStack>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </Box>
    </Box>
  );
};

export default LeaderBoard;