import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "./axiosInstance";

export default function useAuth(requiredRole) {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null); // { username, role }
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      setLoading(true);

      try {
        // Retrieve data from localStorage only on the client
        const username = localStorage.getItem("username");
        const role = localStorage.getItem("role");

        if (username && role) {
          // Set user data if authenticated
          setUser({ username, role });
          setIsAuthenticated(true);

          // Redirect based on role if requiredRole is specified
          if (requiredRole && role !== requiredRole) {
            router.push("/find-tournament"); // Or any other path for unauthorized access
          }
        } else {
          router.push("/login");
        }
      } catch (error) {
        console.error("Authentication check failed:", error);
        router.push("/login"); // Redirect on error
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, [router, requiredRole]);

  return { isAuthenticated, user, loading };
}