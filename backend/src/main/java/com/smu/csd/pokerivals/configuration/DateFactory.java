package com.smu.csd.pokerivals.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class DateFactory {

    @Bean
    public ZonedDateTime getToday(){
        return ZonedDateTime.now();
    }
}
