package com.smu.csd.pokerivals;

import com.smu.csd.pokerivals.match.entity.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Async
    public void notifyPlayerOfInvite(String username){
        log.info("Send invite for tournament to {}", username);
    }

    @Async
    public void notifyPlayersOfTiming(Match.MatchId matchId){
        log.info("Tell players about timing update");
    }

    @Async
    public void notifyMatchOutcome(Match.MatchId matchId){
        log.info("Tell players about match outcome");
    }

    @Async
    public void notifyUpdateOfTimingAgreement(Match.MatchId matchId) {
        log.info("Tell admin that got changes to agreement");
    }
}
