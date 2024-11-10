package com.smu.csd.pokerivals.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smu.csd.pokerivals.notification.dto.LambdaNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class NotificationService {

    private final String lambdaArn;
    private final ObjectMapper jacksonObjectMapper;
    private final LambdaAsyncClient lambdaClient;
    private final Environment environment;

    public NotificationService(@Value("${notification.lambda-arn}")String lambdaArn, ObjectMapper jacksonObjectMapper, LambdaAsyncClient lambdaClient, Environment environment) {
        this.lambdaArn = lambdaArn;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.lambdaClient = lambdaClient;
        this.environment = environment;
    }

    @Async
    public void pushNotificationToLambda(LambdaNotificationDTO dto){
        log.info("notifying {}", dto.type());
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")){
            return;
        }

        // send to lambda
        byte[] json = null;

        try {
            json = jacksonObjectMapper.writeValueAsBytes(dto);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException when trying to send notification: {}", e.getMessage());
        }

        byte[] finalJson = json;
        CompletableFuture<InvokeResponse> future = lambdaClient.invoke(b -> {
            b.functionName(lambdaArn)
                    .invocationType(InvocationType.EVENT)
                    .payload(SdkBytes.fromByteArray(
                            finalJson
                    ));
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            log.error("InterruptedException when trying to send notification: {}", e.getMessage());
        } catch (ExecutionException e) {
            log.error("ExecutionException when trying to send notification: {}", e.getMessage());
        }
    }
}
