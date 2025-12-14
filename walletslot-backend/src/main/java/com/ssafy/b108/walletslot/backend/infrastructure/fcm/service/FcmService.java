package com.ssafy.b108.walletslot.backend.infrastructure.fcm.service;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import com.ssafy.b108.walletslot.backend.infrastructure.fcm.util.GoogleAccessTokenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmService {

    // Field
    @Qualifier("fcmWebClient")
    private final WebClient fcmWebClient;

    private final GoogleAccessTokenUtil googleAccessTokenUtil;

    // Method
    public Mono<String> sendMessage(String targetFcmToken, String title, String body) {

        // AccessToken 발급
        String accessToken = googleAccessTokenUtil.getAccessToken();

        // 전송할 메시지 구조 (FCM v1)
        Map<String, Object> message = Map.of(
                "message", Map.of(
                        "token", targetFcmToken,
                        "notification", Map.of(
                                "title", title,
                                "body", body
                        )
                )
        );

        try {
            // WebClient 호출
            return fcmWebClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(message), Map.class)
                    .retrieve()
                    .bodyToMono(String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "FcmService - 000");
        }
    }

    public void sendMessageNotification(String targetFcmToken, Notification notification) {

        // AccessToken 발급
        String accessToken = googleAccessTokenUtil.getAccessToken();

        // 전송할 메시지 구조 (FCM v1)
        Map<String, Object> message = Map.of(
                "message", Map.of(
                        "token", targetFcmToken,
                        "notification", Map.of(
                                "title", notification.getTitle(),
                                "body", notification.getBody()
                        )
                )
        );

        try {
            // WebClient 호출
            fcmWebClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(message), Map.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> {
                                System.out.println("✅ [FCMService] 알림발송 성공 " + response);
                                notification.updateIsDelivered(true);
                            },
                            error -> {
                                System.err.println("❌ [FCMService] 알림발송 실패 " + error.getMessage());
                                notification.updateIsDelivered(false);
                            }
                    );
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "FcmService - 000");
        }
    }
}
