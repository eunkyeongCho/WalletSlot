package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class GetDeviceListResponseDto {
    boolean success;
    String message;
    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {
        List<DeviceDto> devices;
    }
}
