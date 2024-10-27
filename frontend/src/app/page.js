"use client"

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from "./page.module.css";

export default function Home() {
  const router = useRouter();

  // Redirect to login when the page loads
  useEffect(() => {
    router.push('/landing');
  }, [router]);

  return (
    <div className={styles.page}>
    </div>
  );
}