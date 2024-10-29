"use client";

import axios from "axios";

const axiosInstance = axios.create({
    baseURL: process.env.NEXT_PUBLIC_BACKEND_URL , 
    withCredentials: true, // ensures credentials are sent with every request
    headers: {
        "Content-Type": "application/json",
    },
});

export default axiosInstance;
