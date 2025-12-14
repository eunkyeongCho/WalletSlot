package com.ssafy.b108.walletslot.backend.domain.notification.dto.device;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint.Platform;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint.Status;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * 통합 PATCH 요청 DTO (부분 수정)
 * - null이 아닌 필드만 반영
 * - remoteLogout=true 오면 최우선 수행(다른 필드 무시)
 */
@Value
@Builder
@Jacksonized
public class UpdateDeviceRequestDto {
    Boolean remoteLogout;   // 최우선: 원격 로그아웃
    Boolean pushEnabled;    // 푸시 on/off

    @Size(max = 255)
    String token;           // FCM/WebPush 토큰 교체

    Platform platform;      // 플랫폼 변경 (ANDROID/IOS)
    Status status;          // 상태 변경 (ACTIVE/LOGGED_OUT/ACCOUNT_LOCKED/USER_WITHDRAW)
}
