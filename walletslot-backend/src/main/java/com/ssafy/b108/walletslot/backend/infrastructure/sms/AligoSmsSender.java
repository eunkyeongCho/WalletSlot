package com.ssafy.b108.walletslot.backend.infrastructure.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.sms.provider", havingValue = "aligo")
public class AligoSmsSender implements SmsSender {

    private final RestTemplate restTemplate = new RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${app.sms.aligo.key}")
    String apiKey;
    @org.springframework.beans.factory.annotation.Value("${app.sms.aligo.userId}")
    String userId;
    @org.springframework.beans.factory.annotation.Value("${app.sms.aligo.sender}")
    String sender;

    @Override
    public boolean sendText(String to, String text) {
        try {
            String url = "https://apis.aligo.in/send/"; // 알리고 SMS 엔드포인트
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("key", apiKey);
            form.add("user_id", userId);
            form.add("sender", sender);
            form.add("receiver", to);
            form.add("msg", text);
            form.add("msg_type", "SMS");

            ResponseEntity<String> res = restTemplate.postForEntity(url, new HttpEntity<>(form, headers), String.class);
            log.info("[ALIGO] status={}, body={}", res.getStatusCode(), res.getBody());
            return res.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("[ALIGO] send error", e);
            return false;
        }
    }
}
