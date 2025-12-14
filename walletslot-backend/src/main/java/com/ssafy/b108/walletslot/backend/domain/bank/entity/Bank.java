package com.ssafy.b108.walletslot.backend.domain.bank.entity;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bank")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    // Field
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 64, nullable = false)
    private String code;

    @Column(length = 64, nullable = false)
    private String color;

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;
}
