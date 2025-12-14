package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class PullNotificationListResponseDto {

    boolean success;

    String message;

    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {

        java.util.List<NotificationDto> notifications;
    }
}
