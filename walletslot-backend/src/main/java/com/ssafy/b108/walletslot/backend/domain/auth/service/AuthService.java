package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.config.security.JwtProvider; // AT 발급/검증(기존)
import com.ssafy.b108.walletslot.backend.domain.auth.dto.Tokens;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.PepperKey;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.RefreshToken;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.UserPin;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.PepperKeyRepository;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.RefreshTokenRepository;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.UserPinRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.crypto.PasswordUpgrader;
import com.ssafy.b108.walletslot.backend.global.crypto.PepperSecretProvider;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserPinRepository userPinRepository;
    private final PepperKeyRepository pepperKeyRepository;

    private final PepperSecretProvider pepperSecrets;
    private final PasswordUpgrader passwordUpgrader;
    private final JwtProvider jwtProvider;     // Access/Refresh 모두 Provider 사용

    // 문자/티켓
    private final OtpService otpService;
    private final PhoneVerifyTicketService phoneVerifyTicketService;

    // Refresh 토큰 저장
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.bcrypt.cost:12}")
    private int targetCost;

    private static final int MAX_FAILS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    /** ✅ 새 로그인: Access + Refresh 모두 발급 (컨트롤러는 이걸 쓰는 걸 권장) */
    @Transactional
    public Tokens login(String phoneNumber, String rawPin, String deviceId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "가입되지 않은 번호입니다."));

        UserPin up = userPinRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "PIN 정보가 없습니다."));

        Instant now = Instant.now();
        if (up.isLocked(now)) {
            throw new AppException(ErrorCode.PIN_LOCKED, "계정이 잠금 상태입니다. 잠시 후 다시 시도해 주세요.");
        }

        String secret = pepperSecrets.getSecret(up.getPepperKey().getKeyAlias());
        if (!up.matches(rawPin, secret, bcrypt)) {
            up.markFail(MAX_FAILS, LOCK_DURATION, now);
            throw new AppException(ErrorCode.LOGIN_FAILED, "PIN이 올바르지 않습니다.");
        }

        up.markSuccess(now);
        passwordUpgrader.upgradeIfNeeded(up, rawPin, targetCost);

        // AT/RT 발급 + RT 저장
        return issueTokensOnLogin(user, deviceId);
    }

    /** (호환용) Access만 발급하는 기존 API — 필요 시 계속 사용 가능 */
    @Transactional
    public String loginForAccessOnly(String phoneNumber, String rawPin, String deviceId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "가입되지 않은 번호입니다."));

        UserPin up = userPinRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "PIN 정보가 없습니다."));

        Instant now = Instant.now();
        if (up.isLocked(now)) throw new AppException(ErrorCode.PIN_LOCKED, "계정이 잠금 상태입니다. 잠시 후 다시 시도해 주세요.");

        String secret = pepperSecrets.getSecret(up.getPepperKey().getKeyAlias());
        if (!up.matches(rawPin, secret, bcrypt)) {
            up.markFail(MAX_FAILS, LOCK_DURATION, now);
            throw new AppException(ErrorCode.LOGIN_FAILED, "PIN이 올바르지 않습니다.");
        }

        up.markSuccess(now);
        passwordUpgrader.upgradeIfNeeded(up, rawPin, targetCost);

        return jwtProvider.createAccessToken(user.getId(), deviceId);
    }

    /** 로그인 성공 직후 호출: AT/RT 발급 + RT(DB) 저장 */
    // AuthService 내부
    private Tokens issueTokensOnLogin(User user, String deviceId) {
        // Access
        String accessToken = jwtProvider.createAccessToken(user.getId(), deviceId);

        // Refresh 생성 (Provider 이용)
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), deviceId);
        String jti = jwtProvider.extractJti(refreshToken);
        var exp = jwtProvider.extractExpiresAt(refreshToken);
        if (jti == null || exp == null) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "RT 메타 누락");
        }

        // familyId는 DB에서만 관리
        String familyId = java.util.UUID.randomUUID().toString();

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .deviceId(deviceId)
                .familyId(familyId)
                .jti(jti)
                .status(RefreshToken.Status.ACTIVE)
                .expiresAt(java.time.LocalDateTime.ofInstant(exp, java.time.ZoneId.systemDefault()))
                .build());

        return Tokens.of(accessToken, refreshToken);
    }


    /** 헬퍼: phone → userId */
    @Transactional(readOnly = true)
    public Long findUserIdByPhone(String phone) {
        return userRepository.findByPhoneNumber(phone)
                .map(User::getId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "가입되지 않은 번호입니다."));
    }

    /** 2-4. 내 프로필 */
    @Transactional(readOnly = true)
    public User getMyProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    /** 2-5. PIN 재설정 코드 발송(SMS) */
    @Transactional
    public boolean requestPinReset(String phone) {
        userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "가입되지 않은 번호입니다."));
        otpService.send(phone, "PIN_RESET", null);
        return true;
    }

    /** 2-6. PIN 재설정(코드 소비) */
    @Transactional
    public void resetPin(String phone, String resetCode, String newPin) {
        if (!otpService.verify(phone, "PIN_RESET", resetCode))
            throw new AppException(ErrorCode.OTP_INVALID, "인증코드가 올바르지 않습니다.");

        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "가입되지 않은 번호입니다."));
        UserPin up = userPinRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "PIN 정보가 없습니다."));

        PepperKey active = pepperKeyRepository.findFirstByStatusOrderByCreatedAtDesc(PepperKey.Status.ACTIVATE)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "활성화된 pepper가 없습니다."));
        String secret = pepperSecrets.getSecret(active.getKeyAlias());
        up.upgrade(newPin, active, secret, targetCost, bcrypt, Instant.now());
    }

    /** 2-7. PIN 변경(로그인 상태) */
    @Transactional
    public void changePin(Long userId, String currentPin, String newPin) {
        UserPin up = userPinRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "PIN 정보가 없습니다."));

        String secret = pepperSecrets.getSecret(up.getPepperKey().getKeyAlias());
        if (!up.matches(currentPin, secret, bcrypt)) {
            up.markFail(MAX_FAILS, LOCK_DURATION, Instant.now());
            throw new AppException(ErrorCode.PIN_MISMATCH, "현재 PIN이 올바르지 않습니다.");
        }

        PepperKey active = pepperKeyRepository.findFirstByStatusOrderByCreatedAtDesc(PepperKey.Status.ACTIVATE)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "활성화된 pepper가 없습니다."));
        String newSecret = pepperSecrets.getSecret(active.getKeyAlias());
        up.upgrade(newPin, active, newSecret, targetCost, bcrypt, Instant.now());
    }

    private String normalizePurpose(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }

    /** 2-8. 새 기기 검증(선택) */
    @Transactional(readOnly = true)
    public boolean verifyDevice(String phone, String deviceId, String code) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "deviceId는 DEVICE_VERIFY에서 필수입니다.");
        }
        return otpService.verify(phone, "DEVICE_VERIFY", code, deviceId);
    }

    /** 2-9. SMS 발송 */
    @Transactional
    public boolean sendSmsCode(String phone, String purpose, String deviceId) {
        final String p = normalizePurpose(purpose);
        if ("DEVICE_VERIFY".equals(p) && (deviceId == null || deviceId.isBlank())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "DEVICE_VERIFY에는 deviceId가 필요합니다.");
        }
        otpService.send(phone, p, deviceId);
        return true;
    }


    /** 2-10. SMS 검증 */
    @Transactional
    public boolean verifySmsCode(String phone, String purpose, String code) {
        final String p = normalizePurpose(purpose);
        if ("DEVICE_VERIFY".equals(p)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "DEVICE_VERIFY는 /api/auth/device/verify를 사용하세요.");
        }
        return otpService.verify(phone, p, code);
    }

    /** 가입용 티켓 발급 (SIGNUP 전용) */
    public String issueSignupTicket(String phone, String purpose) {
        if (!"SIGNUP".equalsIgnoreCase(purpose))
            throw new AppException(ErrorCode.BAD_REQUEST, "purpose는 SIGNUP이어야 합니다.");
        return phoneVerifyTicketService.issue(phone, "SIGNUP");
    }

    /** 2-11. 액세스 토큰 유효성 점검 */
    @Transactional
    public ValidateResult validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return new ValidateResult(false, null, null);

        String token = authorizationHeader.substring("Bearer ".length());
        boolean ok = jwtProvider.validate(token);
        if (!ok) return new ValidateResult(false, null, null);

        String sub = jwtProvider.extractSubject(token);
        String did = Optional.ofNullable(jwtProvider.extractDeviceId(token)).orElse(null);
        return new ValidateResult(true, sub, did);
    }

    /** 단순 반환용 DTO */
    public static class ValidateResult {
        public final boolean valid; public final String subject; public final String deviceId;
        public ValidateResult(boolean valid, String subject, String deviceId) {
            this.valid = valid; this.subject = subject; this.deviceId = deviceId;
        }
    }
}
