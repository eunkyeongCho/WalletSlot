package com.ssafy.b108.walletslot.backend.domain.auth.repository;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.PhoneVerifyTicket;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.PhoneVerifyTicket.Purpose;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PhoneVerifyTicketRepository extends JpaRepository<PhoneVerifyTicket, Long> {

    /** 발급 정책: 같은 phone/purpose의 미소비 티켓 무효화(선택) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PhoneVerifyTicket t " +
            "WHERE t.phone = :phone AND t.purpose = :purpose AND t.consumedAt IS NULL")
    int invalidateActive(@Param("phone") String phone, @Param("purpose") Purpose purpose);

    /** 만료 청소(옵션) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PhoneVerifyTicket t WHERE t.expiresAt <= CURRENT_TIMESTAMP")
    int purgeExpired();

    /** 일회성 소비: 조건 만족 시점에만 consumedAt 세팅 (rows=1이면 성공) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PhoneVerifyTicket t SET t.consumedAt = CURRENT_TIMESTAMP " +
            "WHERE t.tokenHash = :hash AND t.phone = :phone AND t.purpose = :purpose " +
            "AND t.consumedAt IS NULL AND t.expiresAt > CURRENT_TIMESTAMP")
    int consumeAtomic(@Param("hash") byte[] hash,
                      @Param("phone") String phone,
                      @Param("purpose") Purpose purpose);
}
