package com.smu.csd.pokerivals;

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
}
