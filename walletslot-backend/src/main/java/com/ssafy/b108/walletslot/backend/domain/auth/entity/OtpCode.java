package com.ssafy.b108.walletslot.backend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
        name = "otp_code",
        uniqueConstraints = @UniqueConstraint(
                name="uq_otp_key",
                columnNames = {"phone","purpose","device_id"}
        )
)
public class OtpCode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=20)
    private String phone;                // 숫자만 저장 권장

    @Column(nullable=false, length=30)
    private String purpose;              // LOGIN / DEVICE_VERIFY / PIN_RESET / SIGNUP

    @Column(name="device_id", nullable=false, length=100)
    private String deviceId;             // DEVICE_VERIFY만 실제 값, 그 외 "-"

    @Column(name="code_hash", nullable=false, length=64)
    private String codeHash;             // HMAC-SHA256(Base64URL, no padding)

    @Column(name="issued_at", nullable=false)
    private LocalDateTime issuedAt;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(nullable=false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(nullable=false)
    @Builder.Default
    private Boolean sent = false;        // 발송 성공 시 true

    public enum Status { PENDING, SENT, USED, EXPIRED }

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    @Builder.Default
    private Status status = Status.PENDING;
}
