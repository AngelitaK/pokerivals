import { Flex, Spinner, Heading } from '@chakra-ui/react';

const LoadingOverlay = () => {
    return (
        <Flex
            position="fixed"
            top="0"
            left="0"
            right="0"
            bottom="0"
            bg="white"
            zIndex="9999"
            align="center"
            justify="center"
        >
            <Heading size="xl" mr={4}>LOADING</Heading>
            <Spinner size="xl" />
        </Flex>
    );
};

export default LoadingOverlay;