package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReplaceTokenRequestDto {
    @NotBlank
    @Size(max = 255)
    String token;
}
