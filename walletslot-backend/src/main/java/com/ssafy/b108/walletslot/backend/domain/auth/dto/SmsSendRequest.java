package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendRequest {
    @Schema(example = "01012345678")
    @NotBlank
    private String phone;

    @Schema(
            description = "인증 목적 ENUM",
            example = "LOGIN",
            allowableValues = {"LOGIN", "DEVICE_VERIFY", "PIN_RESET", "SIGNUP"}
    )
    @NotBlank
    private String purpose;

    @Schema(description = "기기 검증 등 용도에 따라 포함", example = "A1B2C3D4")
    private String deviceId;
}
