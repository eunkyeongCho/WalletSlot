package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class GetNotificationPageResponseDto {

    boolean success;

    String message;

    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {

        java.util.List<NotificationDto> content;

        int page;

        int size;

        long totalElements;

        int totalPages;
    }
}
