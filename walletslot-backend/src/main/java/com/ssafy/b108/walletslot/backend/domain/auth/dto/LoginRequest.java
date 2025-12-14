package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LoginRequest {

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank
    String phone;

    @Schema(description = "PIN(6자리 숫자)", example = "123456")
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN은 숫자 6자리여야 합니다.")
    String pin;

    @Schema(description = "디바이스 식별자", example = "A1B2C3D4")
    @NotBlank
    String deviceId;
}
