package com.ssafy.b108.walletslot.backend.domain.user.repository;

import com.ssafy.b108.walletslot.backend.domain.user.entity.Email;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    // ====== [원본 메서드들] ======
    Optional<Email> findByUserAndEmail(User user, String email);

    Optional<Email> findFirstByUserIdOrderByIdDesc(Long userId);

    Optional<Email> findFirstByUserIdAndNameOrderByIdDesc(Long userId, String name);

    Optional<Email> findFirstByNameOrderByIdDesc(String name);

    /** 현재 기본 이메일(있으면 1건) */
    Optional<Email> findFirstByUserAndPrimaryTrue(User user);

    /** 기존 기본 이메일 일괄 해제 (primary → false) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Email e set e.primary = false where e.user = :user and e.primary = true")
    int clearPrimary(User user);

    Optional<Email> findByUser(User user);

    Optional<Email> findByName(String userName);
}
