'use client'

import {
  Box,
  Flex,
  Button,
  useColorModeValue,
  Stack,
} from '@chakra-ui/react'
import Link from 'next/link'
import Image from 'next/image'
import PokeBall from '../../public/PokeBall.png'


const Logo = () => {
    return (
      <Image src={PokeBall} alt="PokeBall" height={40} />
    );
  };

export default function Navbar() {

  return (
    <Box bg="#FFC020" zIndex={'5'} px={20} top={'0'} position={'sticky'}>
      <Flex h={16} alignItems={'center'} justifyContent={'space-between'}>
        <Link href={'/'}>
          <Box color={'white'}>
            <Logo/>
          </Box>
        </Link>
        <Flex alignItems={'center'}>
        </Flex>
      </Flex>
    </Box>
  )
}