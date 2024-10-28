import axios from "axios";

const axiosInstance = axios.create({
    baseURL: "http://localhost:8080", 
    withCredentials: true, // ensures credentials are sent with every request
    headers: {
        "Content-Type": "application/json",
    },
});

export default axiosInstance;
