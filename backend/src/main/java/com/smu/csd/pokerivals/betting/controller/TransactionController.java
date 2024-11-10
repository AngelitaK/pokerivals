package com.smu.csd.pokerivals.betting.controller;

import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/transaction")
@CrossOrigin
public class TransactionController {


    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("")
    @Operation(summary = "Get all of the user's transactions exclude those who have no effect")
    public TransactionPageDTO getAllTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize
    ){
        return transactionService.getAllTransactions(userDetails.getUsername(), page,pageSize);
    }


}
