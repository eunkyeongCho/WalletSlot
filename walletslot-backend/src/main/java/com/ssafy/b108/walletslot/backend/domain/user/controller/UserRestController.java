package com.ssafy.b108.walletslot.backend.domain.user.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MeBaseDayResponseDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MePatchRequestDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MeResponseDto;
import com.ssafy.b108.walletslot.backend.domain.user.service.UserService;
import com.ssafy.b108.walletslot.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User")
@SecurityRequirement(name = "bearerAuth")
public class UserRestController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "9-1-1 내 정보 조회",
            description = """
                    현재 로그인한 사용자의 프로필 정보를 반환합니다.
                    - email은 email 이력 테이블에서 primary=true 우선, 없으면 가장 최근 이력을 사용합니다.
                    - gender/job은 ENUM 이름 문자열로 반환됩니다.
                    - birthDate는 yyyy-MM-dd 문자열로 반환됩니다.
                    """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "1"))
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MeResponseDto.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "OK",
                                              "data": {
                                                "id": 1,
                                                "uuid": "3c8b8e9e-3d30-4b1b-9b1a-85b3c9f0a1cd",
                                                "name": "홍길동",
                                                "phoneNumber": "01012345678",
                                                "email": "user@example.com",
                                                "gender": "FEMALE",
                                                "birthDate": "2000-02-24",
                                                "baseDay": 26,
                                                "job": "STUDENT",
                                                "createdAt": "2025-09-01T10:20:30",
                                                "updatedAt": "2025-09-10T09:05:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<MeResponseDto> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getMe(principal.userId()));
    }

    @PatchMapping("/me")
    @Operation(
            summary = "9-1-2 내 정보 수정(통합)",
            description = """
                    값이 null인 필드는 변경하지 않습니다.
                    - 전화번호 변경: phoneVerificationToken 검증 필요
                    - 이메일 변경: emailVerificationToken 검증 → 기존 primary 해제 → 동일 주소 이력 있으면 승격, 없으면 append 후 primary 지정(+ verifyNow)
                    """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "2")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "수정할 필드만 포함하세요 (null은 변경 없음).",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MePatchRequestDto.class),
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                            {
                                              "name": "김철수",
                                              "birthDate": "1999-07-15",
                                              "gender": "MAN",
                                              "job": "OFFICE_WORKER",
                                              "baseDay": 26,
                                              "phoneNumber": "01098765432",
                                              "phoneVerificationToken": "PN-123456",
                                              "email": "new@example.com",
                                              "emailVerificationToken": "EM-654321"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MeResponseDto.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "수정 성공",
                                              "data": {
                                                "id": 1,
                                                "uuid": "3c8b8e9e-3d30-4b1b-9b1a-85b3c9f0a1cd",
                                                "name": "김철수",
                                                "phoneNumber": "01098765432",
                                                "email": "new@example.com",
                                                "gender": "MAN",
                                                "birthDate": "1999-07-15",
                                                "baseDay": 26,
                                                "job": "OFFICE_WORKER",
                                                "createdAt": "2025-09-01T10:20:30",
                                                "updatedAt": "2025-09-25T11:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청(ENUM 값/포맷 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "검증 토큰 불일치 등 충돌")
    })
    public ResponseEntity<MeResponseDto> patchMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid MePatchRequestDto request
    ) {
        return ResponseEntity.ok(userService.patchMe(principal.userId(), request));
    }

    @GetMapping("/me/base-day")
    @Operation(
            summary = "9-1-3 내 기준일 조회",
            description = "User의 기준일(baseDay)만 반환합니다.",
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "3"))
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "기준일 조회 성공",
                                              "data": {
                                                "baseDay": 26
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ApiResponse<MeBaseDayResponseDto> getMyBaseDay(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(userService.getMeBaseDay(principal.userId()));
    }
}
