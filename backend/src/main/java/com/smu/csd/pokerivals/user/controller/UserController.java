package com.smu.csd.pokerivals.user.controller;

import com.smu.csd.pokerivals.security.AuthenticationController;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/me")
    public AuthenticationController.WhoAmI getWhoAmI(@AuthenticationPrincipal UserDetails userDetails){
        return new AuthenticationController.WhoAmI(userDetails.getUsername(), userDetails.getAuthorities().toArray(new GrantedAuthority[]{})[0].getAuthority());
    }
}
