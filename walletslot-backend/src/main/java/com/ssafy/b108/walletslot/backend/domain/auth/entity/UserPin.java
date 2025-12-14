package com.ssafy.b108.walletslot.backend.domain.auth.entity;

import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.Instant;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "user_pin")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPin {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pepper_id", nullable = false)
    private PepperKey pepperKey;

    @Column(nullable = false)
    private String bcryptedPin;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int cost;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int failedCount;                      // ← int 로 변경

    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private LocalDateTime lastChangedAt;

    @Column(nullable = false)
    private LocalDateTime lastVerifiedAt;

    /** INSERT 직전 기본값 보정 (DB DEFAULT 대신 JPA에서 채워줌) */
    @PrePersist
    void _prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (lastChangedAt == null)   lastChangedAt = now;
        if (lastVerifiedAt == null)  lastVerifiedAt = now;
        // failedCount는 int라 기본 0
    }

    /* ===== 도메인 규칙 ===== */

    public boolean isLocked(Instant now) {
        return lockedUntil != null &&
                now.isBefore(lockedUntil.atZone(ZoneId.systemDefault()).toInstant());
    }

    public boolean matches(String rawPin, String pepperSecret, BCryptPasswordEncoder bcrypt) {
        return bcrypt.matches(pepperSecret + rawPin, bcryptedPin);
    }

    public void markFail(int maxFails, Duration lockDuration, Instant now) {
        int fails = this.failedCount + 1;
        if (fails >= maxFails) {
            this.lockedUntil = LocalDateTime.ofInstant(now.plus(lockDuration), ZoneId.systemDefault());
            this.failedCount = 0;
        } else {
            this.failedCount = fails;
        }
    }

    public void markSuccess(Instant now) {
        this.failedCount = 0;
        this.lockedUntil = null;
        this.lastVerifiedAt = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    }

    public void upgrade(String rawPin,
                        PepperKey newPepperKey, String newPepperSecret, int newCost,
                        BCryptPasswordEncoder bcrypt, Instant now) {
        this.bcryptedPin   = bcrypt.encode(newPepperSecret + rawPin);
        this.pepperKey     = newPepperKey;
        this.cost          = newCost;
        this.lastChangedAt = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    }
}

