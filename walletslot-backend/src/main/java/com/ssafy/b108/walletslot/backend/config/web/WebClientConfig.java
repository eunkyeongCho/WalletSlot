package com.ssafy.b108.walletslot.backend.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient ssafyGmsWebClient(@Value("${api.ssafy.gms.key}") String ssafyGmsKey) {
        return WebClient.builder()
                .baseUrl("https://gms.ssafy.io/gmsapi/api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ssafyGmsKey)
                .build();
    }

    @Bean
    public WebClient gptWebClient(@Value("${api.haeji.openai.key}") String openAiKey) {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiKey)
                .build();
    }

    @Bean
    public WebClient fcmWebClient(@Value("${fcm.server.project-id}") String projectId) {
        return WebClient.builder()
                .baseUrl("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
