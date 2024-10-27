package com.smu.csd.pokerivals.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${frontend.origin}")
	private String origin;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration configuration = new CorsConfiguration();
				configuration.setAllowedOrigins(Collections.singletonList(origin));
				configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"));
				configuration.setAllowCredentials(true);
				configuration.setAllowedHeaders(Arrays.asList("Authorization", "Requestor-Type", "Cookie", "Content-Type"));
				configuration.setExposedHeaders(Arrays.asList("X-Get-Header", "Set-Cookie"));
			return configuration;
		}))
			.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/auth/**").permitAll()
				.requestMatchers("/player").permitAll()
					.requestMatchers("/admin/link").permitAll()
					.requestMatchers("/error").permitAll()
					.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
					.requestMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated()

			)
			.csrf(AbstractHttpConfigurer::disable);


		return http.build();
	}

	@Bean 
	public SecurityContextRepository securityContextRepository(){
		return new HttpSessionSecurityContextRepository();
	}


    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }



}
