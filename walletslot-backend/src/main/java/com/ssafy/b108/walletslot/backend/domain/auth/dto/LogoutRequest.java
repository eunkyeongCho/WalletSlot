package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @NotBlank
    private String refreshToken;

    @Schema(example = "A1B2C3D4")
    @NotBlank
    private String deviceId;

    @Schema(description = "전체 기기 로그아웃 여부", example = "false")
    private boolean allDevices;
}
