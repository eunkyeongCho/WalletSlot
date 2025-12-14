package com.ssafy.b108.walletslot.backend.domain.auth.repository;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.UserPin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPinRepository extends JpaRepository<UserPin, Long> {
    Optional<UserPin> findByUser_Id(Long userId);
}
