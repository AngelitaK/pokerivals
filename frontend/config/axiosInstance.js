"use client";

import axios from "axios";
console.log("axiosInstance.js", process.env.NEXT_PUBLIC_BACKEND_URL);


const axiosInstance = axios.create({
    baseURL: process.env.NEXT_PUBLIC_BACKEND_URL , 
    withCredentials: true, // ensures credentials are sent with every request
    headers: {
        "Content-Type": "application/json",
    },
});

export default axiosInstance;