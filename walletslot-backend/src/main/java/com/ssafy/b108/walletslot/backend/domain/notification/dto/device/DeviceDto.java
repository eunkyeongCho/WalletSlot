package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint.Platform;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint.Status;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeviceDto {
    String deviceId;
    Platform platform;
    Status status;
    Boolean pushEnabled;
    Boolean tokenPresent; // 토큰 원문은 절대 노출하지 않음
}
