package com.ssafy.b108.walletslot.backend.domain.notification.entity;

import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    public enum Type { SYSTEM, DEVICE, BUDGET, UNCATEGORIZED, SLOT, TRANSACTION, MARKETING }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255, nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TINYTEXT")
    private String body;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Type type = Type.SYSTEM;

    private LocalDateTime readAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDelivered = false;

    private LocalDateTime deliveredAt;

    @Column(name = "tx_id")
    private Long txId;

    public void markDelivered() {
        if (Boolean.TRUE.equals(this.isDelivered)) return;
        this.isDelivered = true;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markRead() {
        if (this.isRead) return;
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void updateIsDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
        this.deliveredAt = LocalDateTime.now();
    }
}
