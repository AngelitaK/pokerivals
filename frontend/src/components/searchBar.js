/* eslint-disable react/prop-types */
import { useState } from 'react';
import { Box, Input, InputGroup, Button, InputRightElement } from '@chakra-ui/react';
import { SearchIcon } from '@chakra-ui/icons';

function SearchBar({ handleSearch }) {  // Updated to receive handleSearch
  const [inputValue, setInputValue] = useState('');

  const handleInputChange = (e) => {
    const value = e.target.value;
    setInputValue(value);
    handleSearch(value); // Call the handleSearch function directly
  };

  return (
    <Box width='450px'>
      <InputGroup size='md'>
        <Input
          pr='4.5rem'
          placeholder='Search friends...'
          borderRadius={20}
          focusBorderColor='black'
          value={inputValue}
          onChange={handleInputChange}
          color='black'
        />
        <InputRightElement width='3rem' bg='inherit'>
          <Button h='1.75rem' size='sm' borderRadius={30}>
            <SearchIcon />
          </Button>
        </InputRightElement>
      </InputGroup>
    </Box>
  );
}

export default SearchBar;
