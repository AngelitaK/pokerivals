"use client";

import { useEffect, useState } from "react";
import { useRouter } from 'next/router';
import { Providers } from "./Providers";
import "./globals.css";
import Footer from "../components/footer";
import { usePathname } from "next/navigation";
import Navbar from "@/components/navbar";
import MainNavbar from "@/components/mainNavbar";

export default function RootLayout({ children }) {
    const currentPath = usePathname()
    console.log(currentPath)

  // Routes where no Navbar should be displayed 
  const noNavbarRoutes = ['/','/landing']; // Homepage route

  // Routes where MainNavbar (NewNavbar) should be displayed
  const mainNavbarRoutes = ['/login', '/activate'];

  return (
    <html lang="en">
      <body>
        <Providers>
          {!noNavbarRoutes.includes(currentPath) && !mainNavbarRoutes.includes(currentPath) && <MainNavbar />}
          {mainNavbarRoutes.includes(currentPath) && <Navbar />}
          {children}
          <Footer />
        </Providers>
      </body>
    </html>
  );
}
