package com.ssafy.b108.walletslot.backend.domain.auth.repository;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.PepperKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PepperKeyRepository extends JpaRepository<PepperKey, Long> {
    Optional<PepperKey> findFirstByStatusOrderByCreatedAtDesc(PepperKey.Status status);
}
