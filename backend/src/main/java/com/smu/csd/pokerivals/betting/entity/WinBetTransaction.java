package com.smu.csd.pokerivals.betting.entity;

import com.smu.csd.pokerivals.match.entity.Match;
import com.smu.csd.pokerivals.match.entity.MatchResult;
import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Entity
@NoArgsConstructor
public class WinBetTransaction extends Transaction{

    public static WinBetTransaction create(Match match, Player player, @Positive long winAmount, ZonedDateTime today){
        if (!match.isBothTeamsFinalisedAndNotNull()
                || match.getMatchResult().equals(MatchResult.PENDING)
                || match.getMatchResult() == MatchResult.CANCELLED
                || match.isForfeited()
        ){
            throw new IllegalArgumentException("Match is not eligible to provide win on");
        }
        return  new WinBetTransaction(match,player, winAmount,today);
    }

    private WinBetTransaction(Match match, Player player, long winAmount, ZonedDateTime today){
        super(player, winAmount,today);
        this.match = match;
    }

    @ManyToOne
    @NotNull
    private Match match;

}
