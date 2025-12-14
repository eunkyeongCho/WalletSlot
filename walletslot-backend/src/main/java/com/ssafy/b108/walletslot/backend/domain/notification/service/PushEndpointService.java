package com.ssafy.b108.walletslot.backend.domain.notification.service;

import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.*;

public interface PushEndpointService {
    RegisterDeviceResponseDto registerDevice(long userId, RegisterDeviceRequestDto request);

    GetDeviceListResponseDto getMyDevices(long userId);

    UpdateDeviceResponseDto updateDevice(long userId, String deviceId, UpdateDeviceRequestDto request);

    ReplaceDeviceTokenResponseDto replaceToken(long userId, String deviceId, ReplaceTokenRequestDto request);

    DeleteDeviceResponseDto deleteDevice(long userId, String deviceId);
}
