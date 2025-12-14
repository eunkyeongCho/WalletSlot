// src/main/java/com/ssafy/b108/walletslot/backend/domain/auth/dto/SmsVerifyRequest.java
package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerifyRequest {
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

    @Schema(example = "123456")
    @NotBlank
    private String code;
}
