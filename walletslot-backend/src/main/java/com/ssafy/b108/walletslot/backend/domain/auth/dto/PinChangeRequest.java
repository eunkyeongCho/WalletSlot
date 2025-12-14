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
public class PinChangeRequest {

    @Schema(description = "현재 PIN(6자리 숫자)", example = "123456")
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN은 숫자 6자리여야 합니다.")
    String currentPin;

    @Schema(description = "새 PIN(6자리 숫자)", example = "654321")
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN은 숫자 6자리여야 합니다.")
    String newPin;
}
