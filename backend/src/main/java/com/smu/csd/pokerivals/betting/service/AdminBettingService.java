package com.smu.csd.pokerivals.betting.service;

import com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.entity.BettingSetting;
import com.smu.csd.pokerivals.betting.repository.*;
import com.smu.csd.pokerivals.configuration.DateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@PreAuthorize("hasAuthority('ADMIN')")
@Service
public class AdminBettingService {
    private final PlaceBetTransactionPagingRepository placeBetTransactionPagingRepository;
    private final PlaceBetTransactionRepository placeBetTransactionRepository;
    private final BettingSettingRepository bettingSettingRepository;
    private final DateFactory dateFactory;
    private final WinBetTransactionRepository winBetTransactionRepository;

    @Autowired
    public AdminBettingService(PlaceBetTransactionPagingRepository placeBetTransactionPagingRepository, PlaceBetTransactionRepository placeBetTransactionRepository, BettingSettingRepository bettingSettingRepository, DateFactory dateFactory, WinBetTransactionPagingRepository winBetTransactionPagingRepository, WinBetTransactionRepository winBetTransactionRepository) {
        this.placeBetTransactionPagingRepository = placeBetTransactionPagingRepository;
        this.placeBetTransactionRepository = placeBetTransactionRepository;
        this.bettingSettingRepository = bettingSettingRepository;
        this.dateFactory = dateFactory;
        this.winBetTransactionRepository = winBetTransactionRepository;
    }

    public List<BettingSetting> getAllSettings(){
        return bettingSettingRepository.findAll();
    }

    public void modifySetting(BettingSetting bs){
        bettingSettingRepository.save(bs);
    }

    public record OngoingBetsSummaryPageDTO(
            List<MatchBettingSummaryDTO> summaries,
            long count
    ){}
    public OngoingBetsSummaryPageDTO getSummariesOfOngoingBets(int page, int limit){
        return new OngoingBetsSummaryPageDTO(
                placeBetTransactionPagingRepository.getBettingSummaryForFutureMatches(dateFactory.getToday(), PageRequest.of(page,limit)),
                placeBetTransactionRepository.countBettingSummaryForFutureMatches(dateFactory.getToday())
        );
    }


}
