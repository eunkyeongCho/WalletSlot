package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DeleteDeviceResponseDto {
    boolean success;
    String message;
    Data data;

    // 삭제 전 스냅샷 반환
    @Value
    @Builder
    @Jacksonized
    public static class Data {
        DeviceDto device;
    }
}
