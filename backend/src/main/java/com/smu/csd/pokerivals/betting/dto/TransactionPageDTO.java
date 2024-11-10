package com.smu.csd.pokerivals.betting.dto;

import com.smu.csd.pokerivals.betting.entity.Transaction;

import java.util.List;

public record TransactionPageDTO(List<Transaction> transactions, long count, long balance){}

