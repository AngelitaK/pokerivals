import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function checkLogged() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      setLoading(true);

      try {
        const username = localStorage.getItem("username");
        const role = localStorage.getItem("role");
        console.log(username, role);

        if (username && role) {
          setIsAuthenticated(true);

          // Redirect based on role if user is authenticated
          if (role == "PLAYER") {
            router.push("/find-tournament");
          } else if (role == "ADMIN") {
            router.push("/admin-home");
          }
        } else {
          console.log("User not logged in");
        }
      }  finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  return { isAuthenticated, loading };
}