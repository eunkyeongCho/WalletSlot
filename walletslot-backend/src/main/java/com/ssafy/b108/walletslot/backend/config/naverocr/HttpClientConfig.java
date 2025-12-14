package com.ssafy.b108.walletslot.backend.config.naverocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(prefix = "naver.ocr", name = "enabled", havingValue = "true")
public class HttpClientConfig {

    @Bean("ocrRestTemplate")
    public RestTemplate ocrRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }
}
