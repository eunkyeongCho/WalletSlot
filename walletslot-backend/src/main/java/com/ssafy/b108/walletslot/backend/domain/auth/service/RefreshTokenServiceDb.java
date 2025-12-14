package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.config.security.JwtProvider;
import com.ssafy.b108.walletslot.backend.domain.auth.dto.Tokens;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.RefreshToken;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.RefreshTokenRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.security.refresh.enabled", havingValue = "true")
public class RefreshTokenServiceDb implements RefreshTokenService {   // ⬅️ implements 추가

    private final JwtProvider jwt;
    private final RefreshTokenRepository repo;

    @Value("${app.security.refresh.rotation:true}")     boolean rotation;    // 사용 시 USED 처리
    @Value("${app.security.refresh.reuse-block:true}")  boolean reuseBlock;  // 재사용 감지 시 패밀리 폐기

    /** 로그인 직후: RT 발급 + DB 저장 */
    @Transactional
    @Override
    public String issueAfterLogin(Long userId, String deviceId) {
        String refresh = jwt.createRefreshToken(userId, deviceId);
        String jti = jwt.extractJti(refresh);
        var exp = jwt.extractExpiresAt(refresh);
        if (jti == null || exp == null) throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "RT 메타 누락");

        String familyId = UUID.randomUUID().toString();

        repo.save(RefreshToken.builder()
                .user(User.builder().id(userId).build())
                .deviceId(deviceId)
                .familyId(familyId)
                .jti(jti)
                .status(RefreshToken.Status.ACTIVE)
                .expiresAt(LocalDateTime.ofInstant(exp, ZoneId.systemDefault()))
                .build());

        return refresh;
    }

    /** RT 회전: oldRefresh → 새 AT/RT */
    @Transactional
    @Override
    public Tokens rotate(String oldRefresh, String deviceId) {
        if (!jwt.validate(oldRefresh)) throw new AppException(ErrorCode.REFRESH_INVALID, "검증 실패");
        if (!Objects.equals("refresh", jwt.extractType(oldRefresh)))
            throw new AppException(ErrorCode.REFRESH_INVALID, "타입 불일치");
        if (!Objects.equals(deviceId, jwt.extractDeviceId(oldRefresh)))
            throw new AppException(ErrorCode.DEVICE_MISMATCH, "디바이스 불일치");

        String jti = jwt.extractJti(oldRefresh);
        Long userId = jwt.extractUserId(oldRefresh);
        if (jti == null || userId == null) throw new AppException(ErrorCode.REFRESH_INVALID, "클레임 누락");

        RefreshToken rt = repo.findByJtiForUpdate(jti)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_INVALID, "미등록 RT"));

        if (!rt.isActive()) {
            if (reuseBlock) repo.revokeFamily(rt.getFamilyId());
            throw new AppException(ErrorCode.REFRESH_INVALID, "재사용/폐기 RT");
        }
        if (rt.isExpired(LocalDateTime.now())) {
            rt.revoke(); repo.save(rt);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "RT 만료");
        }
        if (!Objects.equals(rt.getDeviceId(), deviceId)) {
            throw new AppException(ErrorCode.DEVICE_MISMATCH, "디바이스 불일치");
        }

        if (rotation) {
            rt.markUsed();
            repo.save(rt);
        }

        String newAt = jwt.createAccessToken(userId, deviceId);
        String newRt = jwt.createRefreshToken(userId, deviceId);

        String newJti = jwt.extractJti(newRt);
        var newExp = jwt.extractExpiresAt(newRt);
        repo.save(RefreshToken.builder()
                .user(rt.getUser())
                .deviceId(deviceId)
                .familyId(rt.getFamilyId())
                .jti(newJti)
                .status(RefreshToken.Status.ACTIVE)
                .expiresAt(LocalDateTime.ofInstant(newExp, ZoneId.systemDefault()))
                .rotatedFromJti(rt.getJti())
                .build());

        return Tokens.of(newAt, newRt);
    }

    /** 단일 RT 폐기 (로그아웃) */
    @Transactional
    @Override
    public void revoke(String refresh, String deviceId) {
        if (!jwt.validate(refresh)) return;
        String jti = jwt.extractJti(refresh);
        if (jti == null) return;

        repo.findByJtiForUpdate(jti).ifPresent(rt -> {
            if (Objects.equals(rt.getDeviceId(), deviceId)) {
                rt.revoke();
                repo.save(rt);
            }
        });
    }
}
