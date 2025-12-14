// domain/auth/entity/RefreshToken.java
package com.ssafy.b108.walletslot.backend.domain.auth.entity;

import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token",
        indexes = @Index(name="idx_user_device_status", columnList="user_id, device_id, status"))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    public enum Status { ACTIVE, USED, REVOKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="device_id", length=100, nullable=false)
    private String deviceId;

    @Column(name="family_id", length=36, nullable=false)
    private String familyId;

    @Column(name="jti", length=36, nullable=false, unique=true)
    private String jti;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private Status status;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="rotated_from_jti", length=36)
    private String rotatedFromJti;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public void markUsed() {
        status = Status.USED;
        lastUsedAt = LocalDateTime.now();
    }

    public void revoke() {
        status = Status.REVOKED;
    }
}
