package com.ssafy.b108.walletslot.backend.domain.auth.repository;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name="jakarta.persistence.lock.timeout", value="3000"))
    @Query("select r from RefreshToken r where r.jti = :jti")
    Optional<RefreshToken> findByJtiForUpdate(@Param("jti") String jti);

    @Modifying
    @Query("update RefreshToken r set r.status = 'REVOKED' " +
            "where r.familyId = :familyId and r.status = 'ACTIVE'")
    int revokeFamily(@Param("familyId") String familyId);
}
