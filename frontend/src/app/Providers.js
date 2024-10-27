"use client";

import { CacheProvider } from "@chakra-ui/next-js";
import { createContext, useState } from 'react';
import { ChakraProvider } from "@chakra-ui/react";
import { GoogleOAuthProvider } from "@react-oauth/google";

export const UserContext = createContext();

export function Providers({ children }) {

  const [userData, setUserData] = useState(null); // Store user data globally

  return (
    <GoogleOAuthProvider clientId={process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID}>
      <CacheProvider>
        <ChakraProvider>
          <UserContext.Provider value={{ userData, setUserData }}>
            {children}
          </UserContext.Provider>
        </ChakraProvider>
      </CacheProvider>
    </GoogleOAuthProvider>
  );
}