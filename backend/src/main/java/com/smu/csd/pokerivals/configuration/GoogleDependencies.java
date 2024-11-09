package com.smu.csd.pokerivals.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleDependencies {

    @Value("${google.client-id}")
    private String googleClientId;

    @Bean
	public GoogleIdTokenVerifier googleIdTokenVerifier(){

		return  new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
		// Specify the CLIENT_ID of the app that accesses the backend:
		.setAudience(Collections.singletonList(googleClientId))
		// Or, if multiple clients access the backend:
		//.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
		.build();
	}

}
