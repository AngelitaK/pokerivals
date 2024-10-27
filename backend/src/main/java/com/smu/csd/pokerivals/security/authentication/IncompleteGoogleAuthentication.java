package com.smu.csd.pokerivals.security.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents {@link Authentication} of a user who has <b>not</b> logged in
 */
public class IncompleteGoogleAuthentication implements Authentication{

    private final String token;
    private final DecodedToken decodedToken;

    /**
     * Create based on JWT string from google
     * @param token Token sent by Google
     */
    public IncompleteGoogleAuthentication(String token){
        this.token = token;
        this.decodedToken = DecodedToken.getDecoded(token);
    }

    /**
     * Get Full Name as given by Google JWT
     * @return full name os associated user
     */
    @Override
    public String getName() {
        return decodedToken.getName();
    }

    /**
     * No role yet as not authenticated
     * @return empty collection
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(null);
    }

    /**
     * The JWT string token itself
     * @return JWT string
     */
    @Override
    public Object getCredentials() {
        return token;
    }

    /**
     * No details
     * @return null
     */
    @Override
    public Object getDetails() {
        return null;
    }

    /**
     * Get Full Name as given by Google JWT
     * @return full name os associated user
     */
    @Override
    public Object getPrincipal() {
        return getName();
    }

    /**
     * Authentication is never authenticated
     * @return false
     */
    @Override
    public boolean isAuthenticated() {
        return false;
    }

    /**
     * Token is immutable
     * @exception IllegalArgumentException if trying to set token to authenticated
     */
    @Override
    public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
        if (arg0){
            throw new IllegalArgumentException("Token is never authenticated!");
        }
    }
    
}
