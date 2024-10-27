package com.smu.csd.pokerivals.security.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Class to decode JWT token sent by Google
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecodedToken {

    private String sub;
    private String name;
    private String email;

    /**
     * Create a new DecodedToken
     * @param encodedToken string of JWT sent by google
     * @return new Decoded token made from JWT
     */
    @SneakyThrows
    public static DecodedToken getDecoded(String encodedToken) {
        String[] pieces = encodedToken.split("\\.");
        String b64payload = pieces[1];
        String jsonString = new String(Base64.getDecoder().decode(b64payload), StandardCharsets.UTF_8);

        return new ObjectMapper().readValue(jsonString, DecodedToken.class);
    }

    @SneakyThrows
    public String toString() {
        return  new ObjectMapper().writeValueAsString(this);
    }

}