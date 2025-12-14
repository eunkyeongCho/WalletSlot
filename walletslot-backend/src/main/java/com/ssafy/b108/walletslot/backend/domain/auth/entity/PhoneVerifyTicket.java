package com.ssafy.b108.walletslot.backend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phone_verify_ticket")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PhoneVerifyTicket {

    public enum Purpose { SIGNUP, RESET, ETC }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, columnDefinition = "BINARY(32)")
    private byte[] tokenHash;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Purpose purpose;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime consumedAt;

    @PrePersist
    void prePersist() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
    }
}
