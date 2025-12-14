package com.ssafy.b108.walletslot.backend.domain.consent_form.repository;

import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.ConsentForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentFormRepository extends JpaRepository<ConsentForm, Long> {
    Optional<ConsentForm> findByUuid(String uuid);
}
