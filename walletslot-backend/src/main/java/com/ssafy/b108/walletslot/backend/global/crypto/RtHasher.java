package com.ssafy.b108.walletslot.backend.global.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Refresh Token 원문을 바로 저장하지 않고
 * SHA-256(원문 + pepper)로 해시해 보관/검증하는 유틸.
 * refresh.enabled=true일 때만 빈으로 활성화됨.
 */
@Component
@ConditionalOnProperty(value = "app.security.refresh.enabled", havingValue = "true")
public class RtHasher {

    @Value("${app.security.refresh.hash-pepper}")
    private String pepper;

    /** SHA-256( rawRt + ":" + pepper ) → hex */
    public String sha256Hex(String rawRt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest((rawRt + ":" + pepper).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
