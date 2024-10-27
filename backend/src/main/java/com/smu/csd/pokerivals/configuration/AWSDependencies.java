package com.smu.csd.pokerivals.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;

@Component
public class AWSDependencies {
    @Bean
    public KmsClient kmsClient() { return KmsClient.builder().build();}

    @Bean
    public LambdaAsyncClient lambdaClient()  {return LambdaAsyncClient.create();}
}
