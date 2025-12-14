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
public class PinResetConsumeRequest {

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank
    String phone;

    @Schema(description = "PIN 재설정 코드(예: SMS 6자리)", example = "349201")
    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    String resetCode;

    @Schema(description = "새 PIN(6자리 숫자)", example = "123456")
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN은 숫자 6자리여야 합니다.")
    String newPin;
}
