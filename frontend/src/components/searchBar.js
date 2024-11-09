/* eslint-disable react/prop-types */
import { useState } from 'react';
import { Box, Input, InputGroup, Button, InputRightElement } from '@chakra-ui/react';
import { SearchIcon } from '@chakra-ui/icons';

function SearchBar({ handleSearch }) {
  const [inputValue, setInputValue] = useState('');

  const handleInputChange = (e) => {
    setInputValue(e.target.value);
  };

  const handleButtonClick = () => {
    handleSearch(inputValue); // Trigger search when button is clicked
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleButtonClick();
    }
  };

  return (
    <Box pb={8}>
      <InputGroup size='lg'>
        <Input
          pr='4.5rem'
          placeholder='Search friends...'
          borderRadius={20}
          focusBorderColor='black'
          value={inputValue}
          onChange={handleInputChange}
          onKeyPress={handleKeyPress} 
          color='black'
          border="2px solid black" 
        />
        <InputRightElement width='3rem' bg='inherit'>
          <Button h='1.75rem' size='sm' borderRadius={30} onClick={handleButtonClick}>
            <SearchIcon />
          </Button>
        </InputRightElement>
      </InputGroup>
    </Box>
  );
}

export default SearchBar;
