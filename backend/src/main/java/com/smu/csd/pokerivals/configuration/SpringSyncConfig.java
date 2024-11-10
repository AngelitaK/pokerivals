package com.smu.csd.pokerivals.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("non-async")
public class SpringSyncConfig {  }