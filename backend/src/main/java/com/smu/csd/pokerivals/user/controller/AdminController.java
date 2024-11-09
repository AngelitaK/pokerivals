package com.smu.csd.pokerivals.user.controller;


import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.user.service.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {
    private final AdminService service;

    @Autowired
    public AdminController (AdminService service){
        this.service = service;
    }

    @PostMapping("")
    @Operation(summary="Register admin", description="Registers an admin for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin registered successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Admin failed to be registered.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Message register(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid Admin admin) throws ExecutionException, JsonProcessingException, InterruptedException {
        service.register(userDetails.getUsername(),admin);
        return new Message("Created Admin!");
    }

    @PostMapping("/{username}/google")
    @Operation(summary="Send email to admin to link Google account", description="Send email to admin, by the username, to link their account to a Google account")
    @Parameter(
            name =  "username",
            description  = "Username of the account",
            example = "JohnDoe123",
            required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Email failed to be sent.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Message sendLinkEmail(@PathVariable String username) throws ExecutionException, JsonProcessingException, InterruptedException {
        service.sendLinkEmail(username);
        return new Message("Sent email!");
    }


    @PostMapping("/link")
    @Operation(summary="Link admin account with Google email", description = "Link admin account with Google email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email linked to account successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to link email with account.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Message linkAccount(@RequestBody @Valid AdminService.LinkAccountDTO dto) throws ExecutionException, JsonProcessingException, InterruptedException {
        service.linkEmail(dto);
        return new Message("Updated account");
    }

    @GetMapping("/me/invitee")
    @Operation(summary="Get my invitees", description="Get the users invited by the authenticated admin calling the endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved invitees successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class)) }),
            @ApiResponse(responseCode = "400", description = "Fail to retrieve invitees",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public List<Admin> getInvitees(@AuthenticationPrincipal UserDetails userDetails){
        return service.getInvitees(userDetails.getUsername());

    }


    
}
