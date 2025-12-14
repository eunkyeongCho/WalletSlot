package com.ssafy.b108.walletslot.backend.config.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    // ===== 설정 =====
    @Value("${app.security.jwt.secret_b64}")
    private String secretB64;

    @Value("${app.security.jwt.access-ttl-minutes:15}")
    private long accessTtlMinutes;

    @Value("${app.security.jwt.refresh-ttl-days:30}")
    private long refreshTtlDays;

    @Value("${app.security.jwt.clock-skew-seconds:0}")
    private long clockSkewSeconds;

    // ===== 내부 유틸 =====
    private byte[] key() {
        return Base64.getDecoder().decode(secretB64);
    }

    private SignedJWT parse(String token) throws Exception {
        return SignedJWT.parse(token);
    }

    private boolean verifyAndNotExpired(SignedJWT jwt) throws Exception {
        JWSVerifier verifier = new MACVerifier(key());
        if (!jwt.verify(verifier)) return false;
        Date exp = jwt.getJWTClaimsSet().getExpirationTime();
        if (exp == null) return false;
        return exp.toInstant().isAfter(Instant.now().minusSeconds(clockSkewSeconds));
    }

    private Long readUid(JWTClaimsSet cs) {
        Object uid = cs.getClaim("uid");
        if (uid instanceof Number n) return n.longValue();
        if (uid instanceof String s && !s.isBlank()) {
            try { return Long.valueOf(s); } catch (NumberFormatException ignored) {}
        }
        // 과거 호환: subject에 userId가 들어가 있던 경우
        String sub = cs.getSubject();
        if (sub != null && !sub.isBlank()) {
            try { return Long.valueOf(sub); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // ===== Access =====
    public String createAccessToken(Long userId, String deviceId) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    // 과거 호환을 위해 subject에 userId 유지
                    .subject(String.valueOf(userId))
                    // 신규 권장: 명시적 uid/did
                    .claim("uid", userId)
                    .claim("did", deviceId)
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                    claims
            );
            JWSSigner signer = new MACSigner(key());
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("JWT signing failed", e);
        }
    }

    // ===== Refresh =====
    public String createRefreshToken(Long userId, String deviceId) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(userId))
                    .claim("uid", userId)
                    .claim("did", deviceId)
                    .claim("typ", "refresh")
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(refreshTtlDays * 24 * 60 * 60)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                    claims
            );
            JWSSigner signer = new MACSigner(key());
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("JWT signing failed", e);
        }
    }

    // ===== 검증 =====
    /** 서명 + 만료만 검증 (간단) */
    public boolean validate(String token) {
        try {
            SignedJWT jwt = parse(token);
            return verifyAndNotExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    // ===== Authentication =====
    /** principal = UserPrincipal(uid, did) 로 반환 */
    public Authentication getAuthentication(String token) {
        try {
            SignedJWT jwt = parse(token);
            if (!verifyAndNotExpired(jwt)) return null;

            JWTClaimsSet cs = jwt.getJWTClaimsSet();
            Long uid = readUid(cs);
            if (uid == null) return null;

            String did = cs.getStringClaim("did"); // 없으면 null 허용
            UserPrincipal principal = new UserPrincipal(uid, did);

            // credentials(null), 권한(empty) → 토큰/민감정보 로그 노출 위험 줄임
            return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Claims helpers =====
    public String extractDeviceId(String token) {
        try {
            SignedJWT jwt = parse(token);
            Object did = jwt.getJWTClaimsSet().getClaim("did");
            return (did == null) ? null : String.valueOf(did);
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            SignedJWT jwt = parse(token);
            return readUid(jwt.getJWTClaimsSet());
        } catch (Exception e) {
            return null;
        }
    }

    public String extractType(String token) {
        try {
            SignedJWT jwt = parse(token);
            Object typ = jwt.getJWTClaimsSet().getClaim("typ");
            return (typ == null) ? null : String.valueOf(typ);
        } catch (Exception e) {
            return null;
        }
    }

    public String extractSubject(String token) {
        try {
            return parse(token).getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractJti(String token) {
        try {
            return parse(token).getJWTClaimsSet().getJWTID();
        } catch (Exception e) {
            return null;
        }
    }

    public Instant extractExpiresAt(String token) {
        try {
            Date exp = parse(token).getJWTClaimsSet().getExpirationTime();
            return (exp == null) ? null : exp.toInstant();
        } catch (Exception e) {
            return null;
        }
    }


}
