package com.ssafy.b108.walletslot.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 다대일 — user 삭제 시 DB에서 CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 이름/이메일은 이력 스냅샷 — 수정 불가(updatable = false)로 append-only 보장
    @Column(name = "name", nullable = false, length = 64, updatable = false)
    private String name;

    @Column(name = "email", nullable = false, length = 128, updatable = false)
    private String email;

    // 스키마 컬럼명: is_primary / verified_at / created_at
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===== 도메인 메서드 =====
    public void markPrimary()    { this.primary = true; }
    public void unmarkPrimary()  { this.primary = false; }
    public void verifyNow()      { this.verifiedAt = LocalDateTime.now(); }

    // 편의 메서드: 양방향 연결
    void bindTo(User user) { this.user = user; }

    // 팩토리: 스냅샷 생성
    public static Email snapshot(User user, String name, String email, boolean primary) {
        return Email.builder()
                .user(user)
                .name(name)
                .email(email)
                .primary(primary)
                .build();
    }
}
