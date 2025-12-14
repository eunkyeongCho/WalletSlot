package com.ssafy.b108.walletslot.backend.domain.notification.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.device.*;
import com.ssafy.b108.walletslot.backend.domain.notification.service.PushEndpointService;
import com.ssafy.b108.walletslot.backend.global.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device", description = "디바이스 등록/조회/상태/토큰 관리")
public class DeviceRestController {

    private final PushEndpointService service;

    @PostMapping("/register")
    @Operation(
            summary = "1-1 디바이스 등록/갱신",
            description = """
                현재 단말을 내 계정에 등록하거나 메타/푸시 토큰을 갱신합니다.
                - 같은 deviceId로 재호출 시 갱신(upsert)
                - pushEnabled=false면 푸시 비활성 단말로만 등록
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "1")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterDeviceRequestDto.class),
                            examples = @ExampleObject(name = "req",
                                    value = """
                                    {
                                      "deviceId": "A1B2C3D4",
                                      "deviceName": "Galaxy S24",
                                      "platform": "ANDROID",
                                      "pushEnabled": true,
                                      "pushToken": "fcm_xxx",
                                      "appVersion": "1.0.3",
                                      "osVersion": "14",
                                      "meta": { "brand": "SAMSUNG" }
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록/갱신 성공",
                    content = @Content(schema = @Schema(implementation = RegisterDeviceResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": null,
                                      "data": {
                                        "deviceId": "A1B2C3D4",
                                        "pushEnabled": true,
                                        "registeredAt": "2025-09-24T10:00:00Z"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "badRequest",
                                    value = """
                                    { "success": false, "message": "요청값 검증 실패" }
                                    """))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "unauthorized",
                                    value = """
                                    { "success": false, "message": "로그인이 필요합니다." }
                                    """)))
    })
    public ResponseEntity<RegisterDeviceResponseDto> register(
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal,
            @RequestBody @Valid RegisterDeviceRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registerDevice(principal.userId(), request));
    }

    @GetMapping
    @Operation(
            summary = "1-2 내 디바이스 목록 조회",
            description = "내 계정에 연동된 모든 단말을 조회합니다(최근 등록 우선).",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "2"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetDeviceListResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": null,
                                      "data": {
                                        "devices": [
                                          {
                                            "deviceId": "A1B2C3D4",
                                            "deviceName": "Galaxy S24",
                                            "platform": "ANDROID",
                                            "pushEnabled": true,
                                            "lastSeenAt": "2025-09-24T10:10:00Z"
                                          },
                                          {
                                            "deviceId": "Z9Y8X7W6",
                                            "deviceName": "MacBook",
                                            "platform": "WEB",
                                            "pushEnabled": false,
                                            "lastSeenAt": "2025-09-21T08:00:00Z"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "unauthorized",
                                    value = """
                                    { "success": false, "message": "로그인이 필요합니다." }
                                    """)))
    })
    public ResponseEntity<GetDeviceListResponseDto> list(
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.getMyDevices(principal.userId()));
    }

    @PatchMapping("/{deviceId}")
    @Operation(
            summary = "1-3 디바이스 상태 변경",
            description = """
                푸시 on/off 또는 원격 로그아웃을 수행합니다.
                - request 예) {"pushEnabled":false} 또는 {"remoteLogout":true}
                (전역 동의는 별도 모듈)
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "3")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateDeviceRequestDto.class),
                            examples = @ExampleObject(name = "reqPushOff",
                                    value = """
                                    { "pushEnabled": false }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = UpdateDeviceResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": null,
                                      "data": {
                                        "deviceId": "A1B2C3D4",
                                        "pushEnabled": false,
                                        "remoteLogout": false,
                                        "updatedAt": "2025-09-24T10:20:00Z"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "forbidden",
                                    value = """
                                    { "success": false, "message": "접근 권한이 없습니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "없는 단말",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound",
                                    value = """
                                    { "success": false, "message": "단말을 찾을 수 없습니다." }
                                    """)))
    })
    public ResponseEntity<UpdateDeviceResponseDto> update(
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "관리 대상 deviceId", example = "A1B2C3D4")
            @PathVariable String deviceId,
            @RequestBody @Valid UpdateDeviceRequestDto request
    ) {
        return ResponseEntity.ok(service.updateDevice(principal.userId(), deviceId, request));
    }

    @PostMapping("/{deviceId}/token")
    @Operation(
            summary = "1-4 FCM/WebPush 토큰 교체",
            description = "해당 단말의 푸시 토큰을 교체(재발급)합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "4")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ReplaceTokenRequestDto.class),
                            examples = @ExampleObject(name = "req",
                                    value = """
                                    { "pushToken": "fcm_new_xxx" }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "교체 성공",
                    content = @Content(schema = @Schema(implementation = ReplaceDeviceTokenResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": null,
                                      "data": {
                                        "deviceId": "A1B2C3D4",
                                        "pushToken": "fcm_new_xxx",
                                        "replacedAt": "2025-09-24T10:30:00Z"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "중복/충돌",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "conflict",
                                    value = """
                                    { "success": false, "message": "중복된 요청입니다." }
                                    """)))
    })
    public ResponseEntity<ReplaceDeviceTokenResponseDto> replaceToken(
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "관리 대상 deviceId", example = "A1B2C3D4")
            @PathVariable String deviceId,
            @RequestBody @Valid ReplaceTokenRequestDto request
    ) {
        return ResponseEntity.ok(service.replaceToken(principal.userId(), deviceId, request));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(
            summary = "1-5 디바이스 삭제(연동 해지)",
            description = "단말과 계정 연동을 해제합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "5"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = DeleteDeviceResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": null,
                                      "data": { "deviceId": "A1B2C3D4", "deleted": true }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "없는 단말",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound",
                                    value = """
                                    { "success": false, "message": "단말을 찾을 수 없습니다." }
                                    """)))
    })
    public ResponseEntity<DeleteDeviceResponseDto> delete(
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "관리 대상 deviceId", example = "A1B2C3D4")
            @PathVariable String deviceId
    ) {
        return ResponseEntity.ok(service.deleteDevice(principal.userId(), deviceId));
    }
}
