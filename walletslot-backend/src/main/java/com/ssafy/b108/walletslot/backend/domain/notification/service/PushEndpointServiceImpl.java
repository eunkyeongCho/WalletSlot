package com.ssafy.b108.walletslot.backend.domain.notification.service;

import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.*;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint;
import com.ssafy.b108.walletslot.backend.domain.notification.repository.PushEndpointRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PushEndpointServiceImpl implements PushEndpointService {

    private final PushEndpointRepository pushRepo;

    private final UserRepository userRepo;

    /** * 1-1 / 8-1-1 디바이스(엔드포인트) 등록/갱신 */
    @Override
    public RegisterDeviceResponseDto registerDevice(final long userId, final RegisterDeviceRequestDto req) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 001]"));

        final PushEndpoint pe = pushRepo.findByUserAndDeviceId(user, req.getDeviceId())
                .orElseGet(() -> PushEndpoint.create(
                        user, req.getDeviceId(), req.getPlatform(), req.getToken(), req.getPushEnabled()
                ));

        if (pe.getId() != null) {
            pe.refresh(req.getPlatform(), req.getToken(), req.getPushEnabled());
        }

        pushRepo.save(pe);

        return RegisterDeviceResponseDto.builder()
                .success(true)
                .message("[DeviceService - 001] 디바이스 등록/갱신 성공")
                .data(RegisterDeviceResponseDto.Data.builder().device(toDto(pe)).build())
                .build();
    }

    /** * 1-2 / 8-1-2 내 디바이스(엔드포인트) 목록 조회 */
    @Override
    public GetDeviceListResponseDto getMyDevices(final long userId) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 002]"));

        final List<PushEndpoint> list = pushRepo.findByUserOrderByIdDesc(user);

        return GetDeviceListResponseDto.builder()
                .success(true)
                .message("[DeviceService - 002] 디바이스 목록 조회 성공")
                .data(GetDeviceListResponseDto.Data.builder()
                        .devices(list.stream().map(this::toDto).toList())
                        .build())
                .build();
    }

    /** * 1-3 / 8-1-3 디바이스(엔드포인트) 상태/설정 변경 (통합 PATCH) */
    @Override
    public UpdateDeviceResponseDto updateDevice(final long userId, final String deviceId, final UpdateDeviceRequestDto req) {
        final PushEndpoint pe = pushRepo.findByUserAndDeviceId(
                userRepo.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 003]")),
                deviceId
        ).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 003]"));

        if (Boolean.TRUE.equals(req.getRemoteLogout())) {
            pe.remoteLogout();
        } else {
            if (req.getPushEnabled() != null) pe.updatePushEnabled(req.getPushEnabled());
            if (req.getToken() != null && !req.getToken().isBlank()) pe.replaceToken(req.getToken());
            if (req.getPlatform() != null) pe.changePlatform(req.getPlatform());
            if (req.getStatus() != null) pe.changeStatus(req.getStatus());
        }

        return UpdateDeviceResponseDto.builder()
                .success(true)
                .message("[DeviceService - 003] 디바이스 상태 변경 성공")
                .data(UpdateDeviceResponseDto.Data.builder().device(toDto(pe)).build())
                .build();
    }

    /** * 1-4 (레거시) FCM/WebPush 토큰 교체 */
    @Override
    public ReplaceDeviceTokenResponseDto replaceToken(final long userId, final String deviceId, final ReplaceTokenRequestDto req) {
        final PushEndpoint pe = pushRepo.findByUserAndDeviceId(
                userRepo.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 004]")),
                deviceId
        ).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 004]"));

        pe.replaceToken(req.getToken());

        return ReplaceDeviceTokenResponseDto.builder()
                .success(true)
                .message("[DeviceService - 004] FCM/WebPush 토큰 교체 성공")
                .data(ReplaceDeviceTokenResponseDto.Data.builder().device(toDto(pe)).build())
                .build();
    }

    /** * 1-5 / 8-1-5 디바이스(엔드포인트) 삭제(연동 해지) */
    @Override
    public DeleteDeviceResponseDto deleteDevice(final long userId, final String deviceId) {
        final PushEndpoint pe = pushRepo.findByUserAndDeviceId(
                userRepo.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 005]")),
                deviceId
        ).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[DeviceService - 005]"));

        final DeviceDto snapshot = toDto(pe);

        pushRepo.delete(pe);

        return DeleteDeviceResponseDto.builder()
                .success(true)
                .message("[DeviceService - 005] 디바이스 삭제(연동 해지) 성공")
                .data(DeleteDeviceResponseDto.Data.builder().device(snapshot).build())
                .build();
    }

    // 공통: 엔티티 -> DTO 변환
    private DeviceDto toDto(final PushEndpoint e) {
        return DeviceDto.builder()
                .deviceId(e.getDeviceId())
                .platform(e.getPlatform())
                .status(e.getStatus())
                .pushEnabled(e.isPushEnabled())
                .tokenPresent(e.getToken() != null && !e.getToken().isBlank())
                .build();
    }
}
