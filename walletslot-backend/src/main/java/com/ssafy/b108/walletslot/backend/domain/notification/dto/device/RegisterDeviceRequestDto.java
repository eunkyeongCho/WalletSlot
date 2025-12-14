package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint.Platform;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class RegisterDeviceRequestDto {
    @NotBlank
    @Size(max = 64)
    String deviceId;

    @NotNull
    Platform platform;      // ANDROID / IOS

    @Size(max = 255)
    String token;   // 옵션(등록/갱신 시)

    Boolean pushEnabled;             // null이면 기존값 유지/기본값 적용
}
