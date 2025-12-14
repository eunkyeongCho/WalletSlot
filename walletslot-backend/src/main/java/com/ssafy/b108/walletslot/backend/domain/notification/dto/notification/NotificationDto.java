package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {

    private String uuid;
    private String title;
    private String body;
    private Notification.Type type;
    private boolean isRead;
    private Boolean isDelivered;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;

    private String transactionUuid;

    public static NotificationDto from(Notification n) {
        return NotificationDto.builder()
                .uuid(n.getUuid())
                .title(n.getTitle())
                .body(n.getBody())
                .type(n.getType())
                .isRead(n.isRead())
                .isDelivered(n.getIsDelivered())
                .readAt(n.getReadAt())
                .deliveredAt(n.getDeliveredAt())
                .build();
    }

    public static NotificationDto from(Notification n, String transactionUuid) {
        return NotificationDto.builder()
                .uuid(n.getUuid())
                .title(n.getTitle())
                .body(n.getBody())
                .type(n.getType())
                .isRead(n.isRead())
                .isDelivered(n.getIsDelivered())
                .readAt(n.getReadAt())
                .deliveredAt(n.getDeliveredAt())
                .transactionUuid(transactionUuid)
                .build();
    }
}
