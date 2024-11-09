import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function useAuth(requiredRole) {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null); // { username, role }
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      const username = localStorage.getItem("username");
      const role = localStorage.getItem("role");

      if (username && role) {
        const isRoleAuthorized = Array.isArray(requiredRole)
          ? requiredRole.includes(role)
          : role === requiredRole;

        if (!isRoleAuthorized) {
          router.push(role === "PLAYER" ? "/find-tournament" : "/admin-home");
        } else {
          setUser({ username, role });
          setIsAuthenticated(true);
        }
      } else {
        router.push("/login");
      }
      setLoading(false);
    };

    checkAuth();
  }, [router, requiredRole]);

  return { isAuthenticated, user, loading };
}