// src/main/java/com/ssafy/b108/walletslot/backend/domain/auth/dto/TokenValidateResponse.java
package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TokenValidateResponse {

    @Schema(example = "true")
    private final boolean valid;

    @Schema(description = "토큰 subject(대개 userId)", example = "1")
    private final String subject;

    @Schema(example = "A1B2C3D4")
    private final String deviceId;

    public TokenValidateResponse(boolean valid, String subject, String deviceId) {
        this.valid = valid;
        this.subject = subject;
        this.deviceId = deviceId;
    }

    // 컨트롤러/서비스에서 쓰기 편하게 빌더 제공
    @Builder
    public static TokenValidateResponse of(boolean valid, String subject, String deviceId) {
        return new TokenValidateResponse(valid, subject, deviceId);
    }
}
