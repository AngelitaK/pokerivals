"use client";

import { ChakraProvider } from "@chakra-ui/react";
import { GoogleOAuthProvider } from "@react-oauth/google";

export function Providers({ children }) {

  return (
    <GoogleOAuthProvider clientId={process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID}>
        <ChakraProvider>
            {children}
        </ChakraProvider>
    </GoogleOAuthProvider>
  );
}