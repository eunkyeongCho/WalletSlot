package com.ssafy.b108.walletslot.backend.domain.consent_form.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consent_form")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentForm {

    // Field
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @Lob
    @Column(columnDefinition= "TEXT")
    private String title;

    @OneToMany(mappedBy = "consentForm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserConsent> userConsents;
}
