package com.smu.csd.pokerivals.notification.dto;

import com.smu.csd.pokerivals.user.entity.User;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record NotificationDetails (
        Map<String, String> usernameEmail,
        String tournamentName,
        ZonedDateTime timeToInclude
){
    public static Map<String,String> convertUsersToMap(Collection<? extends User> users){
        return users.stream().collect(HashMap::new, (m, v)->m.put(v.getUsername(), v.getEmail()), HashMap::putAll);
    }
}

