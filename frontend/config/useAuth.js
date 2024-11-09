import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

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
          setIsAuthenticated(true);
          setUser({ username, role });

          // If requiredRole is an array, check if it includes the user's role.
          const isRoleAuthorized = Array.isArray(requiredRole)
            ? requiredRole.includes(role)
            : role === requiredRole;

          if (!isRoleAuthorized) {
            if (role === "PLAYER") {
              router.push("/find-tournament");
            } else if (role === "ADMIN") {
              router.push("/admin-home");
            }
          }
        }
        // user not logged
        else {
          router.push("/login");
        }
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, [router, requiredRole]);

  return { isAuthenticated, user, loading };
}