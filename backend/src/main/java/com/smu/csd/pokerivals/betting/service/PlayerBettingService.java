package com.smu.csd.pokerivals.betting.service;

import com.smu.csd.pokerivals.betting.dto.MatchBettingSummaryDTO;
import com.smu.csd.pokerivals.betting.dto.TransactionPageDTO;
import com.smu.csd.pokerivals.betting.entity.BettingSetting;
import com.smu.csd.pokerivals.betting.entity.BettingSide;
import com.smu.csd.pokerivals.betting.entity.PlaceBetTransaction;
import com.smu.csd.pokerivals.betting.entity.WinBetTransaction;
import com.smu.csd.pokerivals.betting.repository.*;
import com.smu.csd.pokerivals.configuration.DateFactory;
import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.match.repository.MatchRepository;
import com.smu.csd.pokerivals.notification.dto.LambdaNotificationDTO;
import com.smu.csd.pokerivals.notification.dto.NotificationDetails;
import com.smu.csd.pokerivals.notification.dto.NotificationType;
import com.smu.csd.pokerivals.notification.service.NotificationService;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

@Slf4j
@Service

public class PlayerBettingService {
    private final PlayerRepository playerRepository;
    private final DateFactory dateFactory;
    private final MatchRepository matchRepository;
    private final BettingSettingRepository bettingSettingRepository;
    private final NotificationService notificationService;

    @Autowired
    public PlayerBettingService(PlayerRepository playerRepository, DateFactory dateFactory, MatchRepository matchRepository, BettingSettingRepository bettingSettingRepository, NotificationService notificationService, PlaceBetTransactionRepository placeBetTransactionRepository, PlaceBetTransactionPagingRepository placeBetTransactionPagingRepository, WinBetTransactionRepository winBetTransactionRepository, WinBetTransactionPagingRepository winBetTransactionPagingRepository, TransactionRepository transactionRepository) {
        this.playerRepository = playerRepository;
        this.dateFactory = dateFactory;
        this.matchRepository = matchRepository;
        this.bettingSettingRepository = bettingSettingRepository;
        this.notificationService = notificationService;
        this.placeBetTransactionRepository = placeBetTransactionRepository;
        this.placeBetTransactionPagingRepository = placeBetTransactionPagingRepository;
        this.winBetTransactionRepository = winBetTransactionRepository;
        this.winBetTransactionPagingRepository = winBetTransactionPagingRepository;
        this.transactionRepository = transactionRepository;
    }

    private final PlaceBetTransactionRepository placeBetTransactionRepository;
    private final PlaceBetTransactionPagingRepository placeBetTransactionPagingRepository;
    private final WinBetTransactionRepository winBetTransactionRepository;
    private final WinBetTransactionPagingRepository winBetTransactionPagingRepository;
    private final TransactionRepository transactionRepository;

    @PreAuthorize("hasAuthority('PLAYER')")
    public TransactionPageDTO getAllActiveBetsPlacedTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                placeBetTransactionPagingRepository.findByChangeInCentsLessThanAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                placeBetTransactionRepository.countByChangeInCentsLessThanAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }

    @PreAuthorize("hasAuthority('PLAYER')")
    public TransactionPageDTO getAllInactiveBetsPlacedTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                placeBetTransactionPagingRepository.findByChangeInCentsAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                placeBetTransactionRepository.countByChangeInCentsAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }

    @PreAuthorize("hasAuthority('PLAYER')")
    public TransactionPageDTO getAllWinBetTransaction(String playerUsername, int page, int limit){
        return new TransactionPageDTO(
                winBetTransactionPagingRepository.findByChangeInCentsGreaterThanAndPlayer_Username(0, playerUsername, PageRequest.of(page, limit)),
                winBetTransactionRepository.countByChangeInCentsGreaterThanAndPlayer_Username(0, playerUsername),
                transactionRepository.getPlayerBalance(playerUsername)
        );
    }

    public record PlaceOrModifyBetDTO(Match.MatchId matchId, @Positive long betAmountInCents, BettingSide side){}

    @Transactional
    @PreAuthorize("hasAuthority('PLAYER')")
    public void placeBet(String playerUsername, PlaceOrModifyBetDTO dto){
        var player = playerRepository.findById(playerUsername).orElseThrow();
        var match = matchRepository.findById(dto.matchId).orElseThrow();

        if (transactionRepository.getPlayerBalance(playerUsername) >= dto.betAmountInCents) {
            var bet = PlaceBetTransaction.createBet(match,player, dto.betAmountInCents, dto.side, dateFactory.getToday());
            bet = placeBetTransactionRepository.save(bet);
        }
    }

    public record WinPredictionDTO(double winAmountInCents){};

    @Transactional
    @PreAuthorize("hasAuthority('PLAYER')")
    public WinPredictionDTO predictWinAmount(PlaceOrModifyBetDTO dto){
        var match = matchRepository.findById(dto.matchId).orElseThrow();
        var summary =  placeBetTransactionRepository.getBettingSummaryForOneMatch(dto.matchId).orElse(
                new MatchBettingSummaryDTO(null,0,0,0,0)
        );
        log.info(summary.toString());
        var profitMargin =  BettingSetting.calculateProfitMargin(bettingSettingRepository.findAll(),match.calculateWinRate(
                dto.side == BettingSide.TEAM_A
        ));
        log.info("Profit {}", profitMargin);
        return new WinPredictionDTO(
                ((summary.totalBetsOnTeamAInCents() + summary.totalBetsOnTeamBInCents()) *(1-(profitMargin/100)) )
                        /
                        ((dto.side == BettingSide.TEAM_A ? summary.totalNoOfTeamABets() : summary.totalNoOfTeamBBets())+1));
    }

    @Transactional
    public void winBet(Match.MatchId matchId){
        List<Player> loser = new ArrayList<>();
        List<Player> winners = new ArrayList<>();

        List<WinBetTransaction> winBets = new ArrayList<>();

        var match = matchRepository.findById(matchId).orElseThrow();
        if (match.getMatchResult() == MatchResult.PENDING || match.getMatchResult() == MatchResult.CANCELLED){
            throw new IllegalArgumentException("cannot provide win to pending/cancelled match");
        }
        var summary = placeBetTransactionRepository.getBettingSummaryForOneMatch(matchId).orElse(
                new MatchBettingSummaryDTO(null,0,0,0,0)
        );
        var profitMargin =  BettingSetting.calculateProfitMargin(bettingSettingRepository.findAll(),match.calculateWinRate(
                match.getMatchResult() == MatchResult.TEAM_A
        ));
        if (((match.getMatchResult() == MatchResult.TEAM_A) ? summary.totalNoOfTeamABets() : summary.totalNoOfTeamBBets()) == 0){
            log.info("no bet distributed as nobody can receive it");
        }
        var amountToDistribute =  ((summary.totalBetsOnTeamAInCents() + summary.totalBetsOnTeamBInCents()) *(1-(profitMargin/100)) )
                /
                ((match.getMatchResult() == MatchResult.TEAM_A) ? summary.totalNoOfTeamABets() : summary.totalNoOfTeamBBets());
        log.info("amount to distribute {}", amountToDistribute);
        log.info("amount customer keep {}",1-(profitMargin/100));
        // create win bet for everyone
        List<PlaceBetTransaction> toProcess = placeBetTransactionRepository.findByMatch_MatchId(matchId);
        int noOfPeopleGiven=0;
        for (PlaceBetTransaction pt: toProcess){
            if (
                    (pt.getSide() == BettingSide.TEAM_A && match.getMatchResult() == MatchResult.TEAM_A) ||
                            (pt.getSide() == BettingSide.TEAM_B && match.getMatchResult() == MatchResult.TEAM_B)
            )
            {
                var wt =WinBetTransaction.create(match,pt.getPlayer(),round(amountToDistribute),dateFactory.getToday());
                winBets.add(wt);
                winners.add(pt.getPlayer());
                pt.setWinBetTransaction(wt);
                noOfPeopleGiven++;
            } else{
                loser.add(pt.getPlayer());
            }
        }
        if(noOfPeopleGiven != ((match.getMatchResult() == MatchResult.TEAM_A) ? summary.totalNoOfTeamABets() : summary.totalNoOfTeamBBets())  ){
            throw new IllegalArgumentException("more people given");
        }

        notificationService.pushNotificationToLambda(
                new LambdaNotificationDTO(
                        new NotificationDetails(
                                NotificationDetails.convertUsersToMap(winners),
                                match.getTournament().getName(),
                                dateFactory.getToday()
                        ),
                        NotificationType.BET_PLAYER_WIN
                )
        );
        notificationService.pushNotificationToLambda(
                new LambdaNotificationDTO(
                        new NotificationDetails(
                                NotificationDetails.convertUsersToMap(loser),
                                match.getTournament().getName(),
                                dateFactory.getToday()
                        ),
                        NotificationType.BET_PLAYER_LOSE
                )
        );
    }

    @Transactional
    // must be cancelled/not forfeited
    public void processForfeitOrCancel(Match.MatchId matchId){

        var match = matchRepository.findById(matchId).orElseThrow();
        if (!match.isForfeited() && !match.getMatchResult().equals(MatchResult.CANCELLED)){
            throw new IllegalArgumentException("cannot provide cancel bet of non-forfeited match");
        }

        // create win bet for everyone
        List<PlaceBetTransaction> toProcess = placeBetTransactionRepository.findByMatch_MatchId(matchId);
        for (PlaceBetTransaction pt: toProcess){
            pt.cancelIfForfeited();
        }

        notificationService.pushNotificationToLambda(new LambdaNotificationDTO(
                new NotificationDetails(
                        NotificationDetails.convertUsersToMap(toProcess.stream().map(pt->pt.getPlayer()).toList()),
                        match.getTournament().getName(),
                        dateFactory.getToday()
                ),
                NotificationType.BET_PLAYER_FORFEIT
        ));

        placeBetTransactionRepository.saveAll(toProcess);
    }


}
