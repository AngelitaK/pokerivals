package com.smu.csd.pokerivals.integration;

import com.smu.csd.pokerivals.security.AuthenticationController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IntegrationTestDependency {

    /**
     * Map between sub and the token that has it
     */
    public static Map<String,String> tokenMap = new HashMap<>();

    static {
        tokenMap.put("abc", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhYmMiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjIsImVtYWlsIjoiam9obi5kb2VAc211LmVkdS5zZyJ9.uyC522RGd2NZYu_KqdCLXfmgWzm4CcHmMuUBAqv2AzM");
        tokenMap.put("def","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZWYiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjIsImVtYWlsIjoiam9obi5kb2VAc211LmVkdS5zZyJ9.tO_J1JgR-VergmA62v7YiBY8JWEh7lrjLoe5sIEs54Y");
        tokenMap.put("ghi","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJnaGkiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjIsImVtYWlsIjoiam9obi5kb2VAc211LmVkdS5zZyJ9.Vn2yaAR0wP0aBjR4SYgfPHn7FkePOKt-CSLZ4O-h2ZI");
        tokenMap.put("jkl","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqa2wiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjIsImVtYWlsIjoiam9obi5kb2VAc211LmVkdS5zZyJ9.mheXvwnYgUFjCE67SalHZp99lD5yvul9MiSRFH2QVkA");
        tokenMap.put("mno","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtbm8iLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjIsImVtYWlsIjoiam9obi5kb2VAc211LmVkdS5zZyJ9.KxAQE-igTklU9GMOqV16XrAAW4ZwcALiTzzScYRg6Q8");
    }

    public static Map<String,String> cookies = new HashMap<>();

    /**
     * Store cookie obtained from login
     *
     * @param response HTTP Response from login / me endpoint
     * @return username of cookie/session associated
     */
    public static String storeCookie(ResponseEntity<AuthenticationController.WhoAmI> response){
        String username = response.getBody().username();
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
        cookies.put(username,cookie);

        log.info("Storing cookie {} for user {}", cookie,username);
        return username;
    }

    /**
     * Create an HTTP body with cookie associate with username
     * @param username username of the session (must have been previously stored)
     * @param body data to send (can be null for GET)
     * @return the request object to be sent
     * @param <T> Body class
     */
    public static <T> HttpEntity<T> createStatefulResponse(String username,T body){
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE,cookies.get(username));
        HttpEntity<T> request;
        if (body == null) {
             request = new HttpEntity<>(headers);
        } else {
            request = new HttpEntity<>(body,headers);
        }

        return request;

    }

    public static <T> HttpEntity<T> createStatefulResponse(String username){
        return createStatefulResponse(username,null);
    }

}
