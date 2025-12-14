// src/main/java/com/ssafy/b108/walletslot/backend/domain/auth/dto/RefreshRequest.java
package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = "refreshToken") // 민감정보 로그 노출 방지
public class RefreshRequest {

    @NotBlank
    @Schema(description = "리프레시 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String refreshToken;

    @NotBlank
    @Schema(description = "디바이스 식별자", example = "A1B2C3D4")
    private final String deviceId;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RefreshRequest(
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("deviceId") String deviceId
    ) {
        this.refreshToken = refreshToken;
        this.deviceId = deviceId;
    }

    // 테스트/수동 생성 편의용
    @Builder
    public static RefreshRequest of(String refreshToken, String deviceId) {
        return new RefreshRequest(refreshToken, deviceId);
    }
}
