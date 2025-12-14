package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DeleteNotificationResponseDto {

    boolean success;

    String message;

    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {

        NotificationDto notification; // 삭제 직전 스냅샷
    }
}
