package com.ssafy.b108.walletslot.backend.domain.slot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_slot_decision")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantSlotDecision {

    // Field
    @Id
    @Column(length = 64, nullable = false)
    private String merchantCode;

    @Column(length = 255)
    private String merchantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private Slot slot;   // FK 매핑 (nullable 허용, ON DELETE SET NULL)

    @Column(length = 100)
    private String slotName;

    @Column(precision = 4, scale = 2)
    private BigDecimal slotConfidence;

    @Column(length = 64, nullable = false)
    private String method;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime decidedAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
