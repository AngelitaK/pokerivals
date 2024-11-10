package com.smu.csd.pokerivals.user.controller;

import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.security.authentication.IncompleteGoogleAuthentication;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/player")
@CrossOrigin
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService service){
        this.playerService = service;
    }

    @NoArgsConstructor
    @Getter
    public static class PlayerRegistrationDTO {

        private Player player;

        public PlayerRegistrationDTO(String credentials, Player player) {
            this.credentials = credentials;
            this.player = player;
        }

        @NotEmpty(message = "Google ID token must be provided")
        @NotNull(message = "Google ID cannot be null")
        private String credentials;

        public IncompleteGoogleAuthentication getAuthentication() throws UnsupportedEncodingException{
            return new IncompleteGoogleAuthentication(credentials);
        }
    }

    @PostMapping("")
    @Operation(summary= "Register player",
            description = "Registers a player for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player registered successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Player failed to be registered.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Message register(@RequestBody @Valid PlayerRegistrationDTO dto) throws UnsupportedEncodingException, JdbcSQLIntegrityConstraintViolationException {
        playerService.register(dto.getPlayer(), dto.getAuthentication());
        return new Message("Player registered successfully");
    }

    @PostMapping("me/friend/{username}")
    @Operation(summary= "Add friend by username",
            description = "Logged-in user can add friend by the username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added friend successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to add friend.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Message addFriend(@PathVariable String username,@AuthenticationPrincipal UserDetails userDetails) throws UnsupportedEncodingException{
        log.info(username);
        log.info(userDetails.getUsername());
        playerService.connectAsFriends(userDetails.getUsername(),username);
        return new Message("Become friends successfully");
    }

    @DeleteMapping("me/friend/{username}")
    @Operation(summary= "Remove friend by given username",
            description = "Logged-in user can remove friend by the username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Removed friend successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to remove friend.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Message removeFriend(@PathVariable String username,@AuthenticationPrincipal UserDetails userDetails) throws UnsupportedEncodingException{
        playerService.disconnectAsFriends(userDetails.getUsername(),username);
        return new Message("Removed friends connection successfully");
    }

    @GetMapping("me/friend")
    @Operation(summary= "Get user's friends",
            description = "Get the logged-in user's friends")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get friends successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to get friends.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public PlayerService.PlayerPageDTO getFriends(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Parameter(description = "page of users to get (start from zero)") @RequestParam("page") Integer page,
                                                  @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize){
        return playerService.getFriendsOf(userDetails.getUsername(),page,pageSize);
    }

    @GetMapping("me/non-friend")
    @Operation(summary= "Get user's friends",
            description = "Get the logged-in user's friends")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get friends successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to get friends.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public PlayerService.PlayerPageDTO getNotFriends(@AuthenticationPrincipal UserDetails userDetails,
                                                     @Parameter(description = "page of users to get (start from zero)") @RequestParam("page") Integer page,
                                                     @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize
    ){
        return playerService.getNotFriendsOf(userDetails.getUsername(),page,pageSize);
    }

    @GetMapping("")
    @Operation(summary= "Get users by username",
            description = "Get users by searching for their username (similar match case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get users successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to get users.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public PlayerService.PlayerPageDTO getFriends(@RequestParam String query,
                                                  @Parameter(description = "page of users to get (start from zero)") @RequestParam("page") Integer page,
                                                  @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize
    )
    {
        return playerService.searchPlayersByUsername(query, page, pageSize);
    }

    @GetMapping("{username}")
    @Operation(summary= "Get a player by username",
            description = "Get player by searching for their username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get player successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to get player.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public Player getPlayer(@PathVariable String username) {
        return playerService.getUser(username);
    }

    @PatchMapping("me/clan/{clanName}")
    @Operation(summary= "Set my clan",
            description = "Set my clan to the clan name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Set clan successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed to set clan.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) }),
            @ApiResponse(responseCode = "403", description = "Forbidden access.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class)) })})
    public  Message setClan(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String clanName){
        playerService.addToClan(userDetails.getUsername(),clanName.toLowerCase());
        return new Message("Added to clan successfully");
    }


}