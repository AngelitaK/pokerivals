package com.smu.csd.pokerivals.betting.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Entity
@NoArgsConstructor
public class PlaceBetTransaction extends Transaction{

    public static PlaceBetTransaction createBet(Match match, Player player, @Positive long betAmount, BettingSide bettingSide ,ZonedDateTime today){
        if (!match.isBothTeamsFinalisedAndNotNull() || !match.getMatchResult().equals(MatchResult.PENDING) || match.isPlayerInMatch(player.getUsername())){
            throw new IllegalArgumentException("Match is not eligible to bet on");
        }
        return  new PlaceBetTransaction(match,player, betAmount,bettingSide,today);
    }

    private PlaceBetTransaction(Match match, Player player, long betAmount,BettingSide side ,ZonedDateTime today){
        super(player, -betAmount,today);
        this.betAmount = betAmount;
        this.match = match;
        this.side = side;
    }

    @ManyToOne
    @NotNull
    @JsonIgnore
    private Match match;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private BettingSide side;


    public void cancelIfForfeited(){
        if (match.isForfeited()){
            this.changeInCents=0;
            this.cancelled = true;
        }
    }

    @Positive
    private long betAmount;
    private boolean cancelled = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private WinBetTransaction winBetTransaction;

    public void setWinBetTransaction(WinBetTransaction winBetTransaction) {
        if (match.getMatchResult() == MatchResult.PENDING ||match.getMatchResult() == MatchResult.CANCELLED ){
            throw new IllegalArgumentException();
        }
        this.winBetTransaction = winBetTransaction;
    }
}
