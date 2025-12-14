package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.PhoneVerifyTicket;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.PhoneVerifyTicket.Purpose;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.PhoneVerifyTicketRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PhoneVerifyTicketServiceMySql {

    private final PhoneVerifyTicketRepository repo;
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final SecureRandom RNG = new SecureRandom();

    /** 발급: 평문 토큰 생성(리턴), 해시만 DB 저장 */
    @Transactional
    public String issue(String phone, String purposeStr) {
        final Purpose purpose = Purpose.valueOf(purposeStr.toUpperCase());

        // 정책: 같은 phone/purpose의 기존 미소비 티켓 무효화(선택)
        repo.invalidateActive(phone, purpose);

        final String token = randomToken();
        final byte[] hash = sha256(token);

        final LocalDateTime now = LocalDateTime.now();
        final PhoneVerifyTicket t = PhoneVerifyTicket.builder()
                .tokenHash(hash)
                .phone(phone)
                .purpose(purpose)
                .issuedAt(now)
                .expiresAt(now.plus(TTL))
                .build();

        repo.save(t);
        return token; // 평문은 응답으로만, DB/로그에 남기지 말 것
    }

    /** 소비: 단일 UPDATE로 원자적 1회 사용 보장 */
    @Transactional
    public void consume(String token, String expectedPurposeStr, String expectedPhone) {
        final Purpose purpose = Purpose.valueOf(expectedPurposeStr.toUpperCase());
        final int rows = repo.consumeAtomic(sha256(token), expectedPhone, purpose);

        if (rows == 0) {
            // 어떤 조건이 깨졌는지 세분화하고 싶다면 조회 후 분기 가능(선택)
            throw new AppException(ErrorCode.FORBIDDEN, "유효하지 않거나 만료/재사용된 티켓");
        }
    }

    private static String randomToken() {
        // 128-bit 랜덤 → hex(32자)
        byte[] b = new byte[16];
        RNG.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private static byte[] sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
