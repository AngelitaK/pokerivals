package com.smu.csd.pokerivals.betting.controller;

import com.smu.csd.pokerivals.betting.entity.BettingSetting;
import com.smu.csd.pokerivals.betting.service.AdminBettingService;
import com.smu.csd.pokerivals.record.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/transaction/admin")
@CrossOrigin
public class AdminBettingController {
    private final AdminBettingService adminBettingService;

    public AdminBettingController(AdminBettingService adminBettingService) {
        this.adminBettingService = adminBettingService;
    }

    @GetMapping("/future-bets")
    @Operation(summary="get future bets READ INSIDE PLS", description ="it gives u summary of match and their bets info, please use this to calculate the profit for us.")
    public AdminBettingService.OngoingBetsSummaryPageDTO getFutureBetsDetails(
            @Parameter(description = "page of transaction to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of transaction per page") @RequestParam("limit") Integer pageSize
    ){
       return  adminBettingService.getSummariesOfOngoingBets(page,pageSize);
    }

    @GetMapping("/past-bets")
    @Operation(summary="get past bets READ INSIDE PLS", description ="historical record")
    public AdminBettingService.FinishedBetsSummaryPage getPastBetsDetails(
            @Parameter(description = "page of transaction to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of transaction per page") @RequestParam("limit") Integer pageSize
    ){
        return  adminBettingService.getSummariesOfPastBets(page,pageSize);
    }

    @GetMapping("/configuration")
    @Operation(summary = "Get parameters of betting configuration")
    public List<BettingSetting> getConfigurations(
    ){
        return  adminBettingService.getAllSettings();
    }

    @PatchMapping("/configuration")
    @Operation(summary = "Modify one parameter")
    public Message modifyConfiguration(
            @RequestBody BettingSetting bettingSetting
    ){
        adminBettingService.modifySetting(bettingSetting);
        return new Message("Modified!");
    }
}
