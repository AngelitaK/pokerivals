package com.smu.csd.pokerivals.security.authentication;

import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.entity.User;
import com.smu.csd.pokerivals.security.GoogleAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 *  This class represents successful authentication made by a user
 *  It must only be constructed by {@link GoogleAuthenticationProvider}
 *
 * @author Joshua Sumarlin
 */
public class CompletedGoogleAuthentication implements Authentication{

    private final MyUserDetails myUserDetails;
    private final String googleSub;
    private boolean authenticated= true;
    private final String username;

    /**
     * {@link UserDetails} set as the principal
     * This is to help the controller obtain the signed-in user details
     */
    public class MyUserDetails implements UserDetails{

        public MyUserDetails(User user){
            this.username = user.getUsername();
            if (user instanceof Player){
                authoritiesList.add(
                    new SimpleGrantedAuthority("PLAYER")
                );
            } else if (user instanceof Admin){
                authoritiesList.add(
                    new SimpleGrantedAuthority("ADMIN")
                );
            }
        }

        private String username;
        private Collection<GrantedAuthority> authoritiesList = new ArrayList<>();

        /**
         * Get user role in general
         *
         * @return array with <b>single</b> element which is a {@link SimpleGrantedAuthority} with role Player/User
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authoritiesList;
        }

        /**
         * No password used to authenticate hence return null
         *
         * @return null
         */
        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return username;
        }

        /**
         * Account never expires
         *
         * @return true
         */
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        /**
         * Account never gets locked
         *
         * @return true
         */
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        /**
         * Credentials never expire
         *
         * @return true
         */
        @Override
        public boolean isCredentialsNonExpired() {
           return true;
        }

        /**
         * Account always enabled
         *
         * @return true
         */
        @Override
        public boolean isEnabled() {
            return true;
        }
        
    }

    /**
     * Create new instance
     * @param user User entity object that has logged-in
     */
    public CompletedGoogleAuthentication(User user){
        this.myUserDetails  = new MyUserDetails(user);
        this.googleSub = user.getGoogleSub();
        this.username = user.getUsername();
    }

    /**
     * Get username
     * @return username (not fullname)
     */
    @Override
    public String getName() {
        return username;
    }

    /**
     * Get Role
     * @return array containing role see {@link MyUserDetails#getCredentials()}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return myUserDetails.getAuthorities();
    }

    /**
     * Get Google Subject Identifier (unique among all google accounts)
     * @return google subject identifier (sub in JWT)
     */
    @Override
    public Object getCredentials() {
        return googleSub;
    }

    /**
     * no details
     * @return null
     */
    @Override
    public Object getDetails() {
        return null;
    }

    /**
     * Principal is the {@link  MyUserDetails} object
     * @return myUserDetails of the user
     */
    @Override
    public Object getPrincipal() {
        return myUserDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Token always starts as authenticated, can be unauthenticated later
     *
     * @throws IllegalArgumentException when token is still authenticated and true argument
     */
    @Override
    public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
        if (arg0){
            throw new IllegalArgumentException("Token is already authenticated!");
        } else {
            this.authenticated = arg0;
        }
    }
    
}
