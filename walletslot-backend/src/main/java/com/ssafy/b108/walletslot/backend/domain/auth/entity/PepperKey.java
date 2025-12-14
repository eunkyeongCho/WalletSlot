package com.ssafy.b108.walletslot.backend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "pepper_keys",
        indexes = { @Index(name = "ux_pepper_key_alias", columnList = "key_alias", unique = true),
                @Index(name = "ix_pepper_status",   columnList = "status") })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PepperKey {

    // Enum
    public enum Status { ACTIVATE, RETIRED, REVOKED }

    // Field
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String keyAlias;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime rotatedAt;

    /* 상태 전이 도메인 메서드(필요 시)
    public void activate() { this.status = Status.activate; this.rotatedAt = Instant.now(); }
    public void retire()   { this.status = Status.retired;  this.rotatedAt = Instant.now(); }
    public void revoke()   { this.status = Status.revoked;  this.rotatedAt = Instant.now(); }
    */
}
