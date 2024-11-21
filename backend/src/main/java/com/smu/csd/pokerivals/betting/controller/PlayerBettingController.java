package com.smu.csd.pokerivals.betting.controller;

import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.service.PlayerBettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/transaction/betting")
@CrossOrigin
public class PlayerBettingController {


    private final PlayerBettingService playerBettingService;

    public PlayerBettingController(PlayerBettingService playerBettingService) {
        this.playerBettingService = playerBettingService;
    }

    @GetMapping("/placed")
    @Operation(summary = "Get all of the user's bets either those who are active and those who aren't. Active means the match is not cancelled/have forfeit on either side")
    public TransactionPageDTO getPlaceBetTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "page of transaction to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of transaction per page") @RequestParam("limit") Integer pageSize,
            @Parameter(description = "whether to get active (i.e. ongoing) bets or not") @RequestParam("active") boolean active
    ){
        if (active){
            return playerBettingService.getAllActiveBetsPlacedTransaction(userDetails.getUsername(),page,pageSize);
        }
        return playerBettingService.getAllInactiveBetsPlacedTransaction(userDetails.getUsername(),page,pageSize);
    }

    @GetMapping("/win")
    @Operation(summary = "Get all of the user's bets either those who are active and those who aren't")
    public TransactionPageDTO getWinBetTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "page of transaction to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of transaction per page") @RequestParam("limit") Integer pageSize
    ){
        return playerBettingService.getAllWinBetTransaction(userDetails.getUsername(),page,pageSize);
    }

}
