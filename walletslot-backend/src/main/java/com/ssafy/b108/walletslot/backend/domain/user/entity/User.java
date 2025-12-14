package com.ssafy.b108.walletslot.backend.domain.user.entity;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public enum Gender { FEMALE, MAN }
    public enum Job { STUDENT, HOMEMAKER, OFFICE_WORKER, SOLDIER, SELF_EMPLOYED, FREELANCER, UNEMPLOYED, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @Column(length = 255)
    private String userKey;

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 64, nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private LocalDateTime birthDate;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Short baseDay;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Job job;

    // 이메일 이력 (append-only). User 삭제 시 DB FK CASCADE도 걸려있지만,
    // JPA에서도 orphanRemoval=true 로 일관성 유지.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC, id DESC")
    @Builder.Default
    private List<Email> emails = new ArrayList<>();

    // ===== 도메인 메서드(세터 금지) =====
    public void changeName(String v)               { this.name = v; }
    public void assignUserKey(String v)            { this.userKey = v; }
    public void changePhoneNumber(String v)        { this.phoneNumber = v; }
    public void changeGender(Gender v)             { this.gender = v; }
    public void changeBirthDate(LocalDateTime v)   { this.birthDate = v; }
    public void changeJob(Job v)                   { this.job = v; }
    public void updateBaseDay(Short v)             { this.baseDay = v; }

    // 편의: 이메일 이력 추가(append-only)
    public void addEmailSnapshot(Email e) {
        e.bindTo(this);
        this.emails.add(e);
    }

    @PrePersist
    void ensureDefaults() {
        if (uuid == null || uuid.isBlank()) uuid = UUID.randomUUID().toString();
    }
}
