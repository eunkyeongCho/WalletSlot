package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.OtpCode;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.OtpCodeRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import com.ssafy.b108.walletslot.backend.infrastructure.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OtpServiceImpl implements OtpService {

    private final SmsSender smsSender;
    private final OtpCodeRepository repo;

    @Value("${app.otp.ttl-minutes:3}")
    private int ttlMinutes;

    @Value("${app.otp.secret:change-me}")
    private String otpSecret;

    // ======================= 발송 =======================
    @Override
    @Transactional(readOnly = false)
    public void send(String phoneRaw, String purposeRaw, String deviceIdRaw) {
        final String phone   = normalizePhone(phoneRaw);
        final String purpose = normalizePurpose(purposeRaw);
        final String device  = normalizeDeviceId(purpose, deviceIdRaw); // DEVICE_VERIFY면 deviceId, 그 외 "-"

        final String code = randomDigits(6);
        final String hash = hmac(code);

        // 1) 기존 키 제거(경쟁조건/중복 방지)
        repo.deleteKey(phone, purpose, device);

        // 2) DB에 PENDING 저장
        OtpCode e = OtpCode.builder()
                .phone(phone)
                .purpose(purpose)
                .deviceId(device)
                .codeHash(hash)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .attempts(0)
                .sent(false)
                .status(OtpCode.Status.PENDING)
                .build();
        repo.save(e);

        // 3) 실제 발송
        String msg = String.format("[Wallet Slot] 인증번호 %s (유효 %d분)", code, ttlMinutes);
        try {
            boolean ok = smsSender.sendText(phone, msg); // 이미 숫자만인 phone
            if (!ok) throw new RuntimeException("SMS provider returned false");

            // 4) 성공 → 상태 갱신
            e.setSent(true);
            e.setStatus(OtpCode.Status.SENT);
            log.debug("[OTP:SEND] phone={}, purpose={}, device={}, ttlMin={}", phone, purpose, device, ttlMinutes);

        } catch (Exception ex) {
            // 5) 실패 → 흔적 삭제
            repo.deleteKey(phone, purpose, device);
            log.warn("[OTP:SEND:FAIL] phone={}, purpose={}, device={}, cause={}", phone, purpose, device, ex.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "문자 발송 실패");
        }
    }

    // ======================= 검증(일반) =======================
    @Override
    @Transactional(readOnly = false)
    public boolean verify(String phoneRaw, String purposeRaw, String codePlain) {
        return verify(phoneRaw, purposeRaw, codePlain, null);
    }

    // ======================= 검증(DEVICE_VERIFY 포함) =======================
    @Override
    public boolean verify(String phoneRaw, String purposeRaw, String codePlain, String deviceIdRaw) {
        final String phone   = normalizePhone(phoneRaw);
        final String purpose = normalizePurpose(purposeRaw);
        final String device  = normalizeDeviceId(purpose, deviceIdRaw);

        OtpCode e = repo.findByPhoneAndPurposeAndDeviceId(phone, purpose, device)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID, "코드를 먼저 요청하세요."));

        if (Boolean.FALSE.equals(e.getSent())) {
            throw new AppException(ErrorCode.OTP_INVALID, "코드를 먼저 요청하세요.");
        }
        if (e.getExpiresAt().isBefore(LocalDateTime.now())) {
            e.setStatus(OtpCode.Status.EXPIRED);
            repo.deleteKey(phone, purpose, device);
            throw new AppException(ErrorCode.OTP_EXPIRED, "인증코드가 만료되었습니다.");
        }

        boolean ok = hmac(codePlain).equals(e.getCodeHash());
        if (ok) {
            e.setStatus(OtpCode.Status.USED);
            repo.deleteKey(phone, purpose, device); // 1회용
            log.debug("[OTP:VERIFY] OK phone={}, purpose={}, device={}", phone, purpose, device);
            return true;
        } else {
            e.setAttempts(e.getAttempts() + 1);
            log.debug("[OTP:VERIFY] FAIL phone={}, purpose={}, device={}, attempts={}", phone, purpose, device, e.getAttempts());
            throw new AppException(ErrorCode.OTP_INVALID, "인증코드가 올바르지 않습니다.");
        }
    }

    // ======================= Helpers =======================
    private String normalizePhone(String raw) {
        return raw == null ? "" : raw.replaceAll("\\D", "");
    }
    private String normalizePurpose(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }
    private String normalizeDeviceId(String purposeUpper, String deviceIdRaw) {
        if ("DEVICE_VERIFY".equals(purposeUpper)) {
            return (deviceIdRaw == null || deviceIdRaw.isBlank()) ? "-" : deviceIdRaw.trim();
        }
        return "-";
    }
    private String randomDigits(int len) {
        int base = (int) Math.pow(10, len - 1);
        int v = ThreadLocalRandom.current().nextInt(base, base * 10);
        return String.valueOf(v);
    }
    private String hmac(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(otpSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] h = mac.doFinal(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(h);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
