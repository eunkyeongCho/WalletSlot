package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountUnreadByTypeResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private long count;
        private Notification.Type type;
    }
}
