package com.ssafy.b108.walletslot.backend.domain.notification.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.GetDeviceListResponseDto;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.RegisterDeviceRequestDto;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.RegisterDeviceResponseDto;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.UpdateDeviceRequestDto;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.UpdateDeviceResponseDto;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.DeleteDeviceResponseDto;
import com.ssafy.b108.walletslot.backend.domain.notification.service.PushEndpointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push/endpoints")
@RequiredArgsConstructor
@Tag(name = "PushEndpoint")
public class PushEndpointRestController {

    private final PushEndpointService service;

    @PostMapping
    @Operation(
            summary = "8-1-1 푸시 엔드포인트 등록/갱신(UPSERT)",
            description = "userId+deviceId 기준으로 없으면 생성, 있으면 token/platform/pushEnabled 갱신",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "1"))
    )
    public ResponseEntity<RegisterDeviceResponseDto> register(
            @AuthenticationPrincipal final UserPrincipal principal,
            @RequestBody @Valid final RegisterDeviceRequestDto request
    ) {
        return ResponseEntity.status(201).body(service.registerDevice(principal.userId(), request));
    }

    @GetMapping
    @Operation(
            summary = "8-1-2 내 푸시 엔드포인트 목록",
            description = "최근 등록 우선",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "2"))
    )
    public ResponseEntity<GetDeviceListResponseDto> list(
            @AuthenticationPrincipal final UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getMyDevices(principal.userId()));
    }

    @PatchMapping("/{deviceId}")
    @Operation(
            summary = "8-1-3 푸시 엔드포인트 부분 수정(통합 PATCH)",
            description = "remoteLogout / pushEnabled / token / platform / status 중 제공된 필드만 변경",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "3"))
    )
    public ResponseEntity<UpdateDeviceResponseDto> patch(
            @AuthenticationPrincipal final UserPrincipal principal,
            @PathVariable final String deviceId,
            @RequestBody @Valid final UpdateDeviceRequestDto request
    ) {
        return ResponseEntity.ok(service.updateDevice(principal.userId(), deviceId, request));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(
            summary = "8-1-5 푸시 엔드포인트 삭제",
            description = "계정-기기 연동 해제",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "5"))
    )
    public ResponseEntity<DeleteDeviceResponseDto> delete(
            @AuthenticationPrincipal final UserPrincipal principal,
            @PathVariable final String deviceId
    ) {
        return ResponseEntity.ok(service.deleteDevice(principal.userId(), deviceId));
    }
}
