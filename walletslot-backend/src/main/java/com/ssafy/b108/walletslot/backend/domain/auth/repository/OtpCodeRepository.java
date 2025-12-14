package com.ssafy.b108.walletslot.backend.domain.auth.repository;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByPhoneAndPurposeAndDeviceId(String phone, String purpose, String deviceId);

    @Modifying
    @Query(value = "DELETE FROM otp_code WHERE phone=:p AND purpose=:u AND device_id=:d", nativeQuery = true)
    void deleteKey(@Param("p") String phone, @Param("u") String purpose, @Param("d") String deviceId);

    @Modifying
    @Query(value = "DELETE FROM otp_code WHERE expires_at < :now OR status IN ('USED','EXPIRED')", nativeQuery = true)
    int purgeExpired(@Param("now") LocalDateTime now);
}
