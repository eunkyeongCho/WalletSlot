package com.ssafy.b108.walletslot.backend.domain.account.entity;

import com.ssafy.b108.walletslot.backend.domain.bank.entity.Bank;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.AccountSlot;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    // Field
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    private String alias;

    @Column(nullable = false, length = 255)
    private String encryptedAccountNo;

    @Column(nullable = false)
    @Builder.Default
    private long balance = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPrimary = false;

    @Column(length = 255)
    @Builder.Default
    private Long lastSyncedTransactionUniqueNo = 0L;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime lastSyncedAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountSlot> accountSlots;

    // Method
    public void updateAlias(String alias) {
        this.alias = alias;
    }
    public void updateIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    public void updateLastSyncedTransactionUniqueNo(Long lastSyncedTransactionUniqueNo) { this.lastSyncedTransactionUniqueNo = lastSyncedTransactionUniqueNo; }
    public void updateLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public void updateBalance(long balance) { this.balance = balance; }
}
