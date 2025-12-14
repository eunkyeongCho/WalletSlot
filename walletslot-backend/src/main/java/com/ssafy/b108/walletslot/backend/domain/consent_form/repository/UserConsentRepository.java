package com.ssafy.b108.walletslot.backend.domain.consent_form.repository;

import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent;
import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    Optional<UserConsent> findByUuid(String uuid);

    boolean existsByUserIdAndConsentFormIdAndStatus(Long userId, Long consentFormId, Status status);

    List<UserConsent> findByUserIdOrderByAgreedAtDesc(Long userId);

    List<UserConsent> findByUserIdAndStatusOrderByAgreedAtDesc(Long userId, Status status);
}
