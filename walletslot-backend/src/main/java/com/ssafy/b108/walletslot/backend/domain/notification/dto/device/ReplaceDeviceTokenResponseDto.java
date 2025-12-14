package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReplaceDeviceTokenResponseDto {
    boolean success;
    String message;
    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data { DeviceDto device; }
}
