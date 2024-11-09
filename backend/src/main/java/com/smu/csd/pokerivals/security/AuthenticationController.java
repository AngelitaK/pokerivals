package com.smu.csd.pokerivals.security;

import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.security.authentication.IncompleteGoogleAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthenticationController {

    public record LoginDetails (
            @NotEmpty(message = "Google ID token must be provided")
            @NotNull(message = "Google ID cannot be null")
            String credentials
    ){};

    @Autowired
    private AuthenticationService authenticationService;



    @PostMapping("/login")
    @Operation(summary = "Allows user to login with Google ID Token", description = "client-id of backend must match frontend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Invalid Token"),
            @ApiResponse(responseCode = "400", description = "Invalid Request")
    })
    public WhoAmI authenticateUser(@Valid @RequestBody LoginDetails loginDetails, HttpServletRequest request, HttpServletResponse response) {
        IncompleteGoogleAuthentication token = new IncompleteGoogleAuthentication(loginDetails.credentials());
        
        Authentication authentication = authenticationService.login(token, request, response);

        return new WhoAmI(authentication.getName(), authentication.getAuthorities().toArray(new GrantedAuthority[]{})[0].getAuthority());
    }

    public static record WhoAmI(String username, String role){};

    @GetMapping("/logout")
    public Message customLogout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return new Message("User signed-out successfully.");
    }
    

}
