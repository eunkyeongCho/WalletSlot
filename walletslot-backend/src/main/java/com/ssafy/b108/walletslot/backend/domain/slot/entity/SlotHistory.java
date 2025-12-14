package com.ssafy.b108.walletslot.backend.domain.slot.entity;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "slot_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotHistory {

    // Field
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_slot_id", nullable = false)
    private AccountSlot accountSlot;

    @Column(nullable = false)
    private Long oldBudget;

    @Column(nullable = false)
    private Long newBudget;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime changedAt;
}
