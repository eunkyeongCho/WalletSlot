package com.ssafy.b108.walletslot.backend.domain.notification.entity;

import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
@Entity
@Table(
        name = "push_endpoint",
        uniqueConstraints = @UniqueConstraint(name = "uk_push_user_device", columnNames = {"user_id", "device_id"})
)
public class PushEndpoint {

    public enum Platform { ANDROID, IOS }
    public enum Status   { ACTIVE, LOGGED_OUT, ACCOUNT_LOCKED, USER_WITHDRAW }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 64, nullable = false)
    private String deviceId; // 고유값: 생성 시에만 설정

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "is_push_enabled", nullable = false)
    @Builder.Default
    private boolean pushEnabled = true;

    // ===== 생성 (고유값 보존) =====
    public static PushEndpoint create(
            User user, String deviceId, Platform platform, String token, Boolean pushEnabled
    ) {
        return PushEndpoint.builder()
                .user(user)
                .deviceId(deviceId)
                .platform(platform)
                .token(isBlank(token) ? null : token)
                .status(Status.ACTIVE)
                .pushEnabled(pushEnabled == null ? true : pushEnabled)
                .build();
    }

    // ===== 상태 전이 (의미 있는 메서드만 허용) =====
    /** 재등록/갱신: platform/token 갱신, pushEnabled는 명시된 경우에만 변경 */
    public void refresh(Platform platform, String token, Boolean pushEnabled) {
        this.platform = platform;
        if (!isBlank(token)) this.token = token;
        if (pushEnabled != null) this.pushEnabled = pushEnabled;
        this.status = Status.ACTIVE;
    }

    /** 디바이스 푸시 on/off (Consent는 별도 모듈에서 판단) */
    public void updatePushEnabled(boolean enabled) {
        this.pushEnabled = enabled;
        if (enabled) this.status = Status.ACTIVE; // on이면 ACTIVE로 복귀
        // off일 땐 status 유지(정책에 따라 바꿀 수 있음)
    }

    /** 원격 로그아웃: 토큰 제거 + pushEnabled=false + LOGGED_OUT */
    public void remoteLogout() {
        this.token = null;
        this.pushEnabled = false;
        this.status = Status.LOGGED_OUT;
    }

    /** 토큰 교체: pushEnabled는 건드리지 않고 상태만 ACTIVE로 */
    public void replaceToken(String newToken) {
        this.token = newToken;
        this.status = Status.ACTIVE;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public void changePlatform(Platform newPlatform) {
        if (newPlatform != null && this.platform != newPlatform)
            this.platform = newPlatform;
    }
    public void changeStatus(Status newStatus) {
        if (newStatus != null && this.status != newStatus)
            this.status = newStatus;
    }

}
