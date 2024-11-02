import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function useAuth(requiredRole = null) {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null); // { username, role }
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      setLoading(true);

      try {
        const username = localStorage.getItem("username");
        const role = localStorage.getItem("role");

        if (username && role) {
          setUser({ username, role });
          setIsAuthenticated(true);

          // Scenario 1: Admin only
          if (requiredRole === "ADMIN" && role !== "ADMIN") {
            router.push("/find-tournament");
          }
          // Scenario 2: Player only
          else if (requiredRole === "PLAYER" && role !== "PLAYER") {
            router.push("/admin-home");
          }
          // Scenario 3: Admin and Player only
          else if (requiredRole === "ANY" && role !== "PLAYER" && role !== "ADMIN") {
            router.push("/login");
          }
          // Scenario 4: Redirect authenticated players away from restricted pages
          else if (role === "PLAYER" && ["/landing", "/login", "/activate"].includes(router.pathname)) {
            router.push("/find-tournament");
          }
          // Scenario 5: Redirect authenticated admins away from restricted pages
          else if (role === "ADMIN" && ["/landing", "/login", "/activate"].includes(router.pathname)) {
            router.push("/admin-home");
          }
        } else {
          // Redirect to login if not authenticated and no specific role is required
          if (requiredRole) {
            router.push("/login");
          }
        }
      } catch (error) {
        console.error("Authentication check failed:", error);
        router.push("/login");
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, [router, requiredRole]);

  return { isAuthenticated, user, loading };
}