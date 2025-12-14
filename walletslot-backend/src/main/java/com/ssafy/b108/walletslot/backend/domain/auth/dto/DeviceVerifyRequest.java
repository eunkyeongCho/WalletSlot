package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceVerifyRequest {
    @Schema(example = "01012345678")
    @NotBlank
    private String phone;

    @Schema(example = "A1B2C3D4")
    @NotBlank
    private String deviceId;

    @Schema(example = "654321")
    @NotBlank
    private String code;
}
