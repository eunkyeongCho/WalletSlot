package com.ssafy.b108.walletslot.backend.domain.auth.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.auth.dto.*;
import com.ssafy.b108.walletslot.backend.domain.auth.service.AuthService;
import com.ssafy.b108.walletslot.backend.domain.auth.service.RefreshTokenService;
import com.ssafy.b108.walletslot.backend.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 및 계정 관련 API")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    // 2-1. 로그인(Access만)
    @PostMapping("/login")
    @Operation(
            summary = "2-1. 로그인 (Access 발급)",
            description = """
                전화번호 + PIN(6자리) + deviceId로 Access 토큰을 발급합니다.
                - 인증 목적 ENUM은 사용하지 않음
                - 응답: accessToken
                - 권장: 이후 /login/full 사용
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "1")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                    { "phone": "01012345678", "pin": "123456", "deviceId": "A1B2C3D4" }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Access 토큰 발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = AccessToken.class),
                            examples = @ExampleObject(
                                    value = """
                    { "success": true, "data": { "accessToken": "ey..." }, "message": null }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패(형식 오류 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    { "success": false, "data": null, "message": "PIN은 숫자 6자리여야 합니다." }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(전화번호/핀 불일치, 비활성화 기기 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    { "success": false, "data": null, "message": "자격 증명이 올바르지 않습니다." }
                    """
                            )
                    )
            )
    })
    public ApiResponse<AccessToken> login(@Valid @RequestBody LoginRequest req){
        String at = authService.loginForAccessOnly(req.getPhone(), req.getPin(), req.getDeviceId());
        return ApiResponse.ok(new AccessToken(at));
    }

    // 2-1+. 로그인(Access/Refresh 동시 발급)
    @PostMapping("/login/full")
    @Operation(
            summary = "2-1+. 로그인 (Access/Refresh 동시 발급)",
            description = """
                전화번호 + PIN(6자리) + deviceId로 Access/Refresh를 동시에 발급합니다.
                - Refresh는 서버에 저장/로테이션 관리됩니다.
                - 권장 로그인 경로
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "2")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                    { "phone": "01012345678", "pin": "123456", "deviceId": "A1B2C3D4" }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 2종 동시 발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = Tokens.class),
                            examples = @ExampleObject(
                                    value = """
                    { "success": true, "data": { "accessToken": "ey...", "refreshToken": "ey..." }, "message": null }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패(형식 오류 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(전화번호/핀 불일치, 비활성화 기기 등)"
            )
    })
    public ApiResponse<Tokens> loginFull(@Valid @RequestBody LoginRequest req) {
        Tokens tokens = authService.login(req.getPhone(), req.getPin(), req.getDeviceId());
        return ApiResponse.ok(tokens);
    }

    // 2-2. 재발급 (Refresh → Access/Refresh)
    @PostMapping("/refresh")
    @Operation(
            summary = "2-2. 토큰 재발급 (Refresh → Access/Refresh)",
            description = """
                Refresh 토큰과 deviceId를 제출하면 서버가 Refresh를 로테이션한 뒤 Access/Refresh를 재발급합니다.
                - 클라이언트는 기존 Refresh를 폐기하고 새 Refresh로 교체하세요.
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "3")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RefreshRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                    { "refreshToken": "ey...", "deviceId": "A1B2C3D4" }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = Tokens.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패(형식 오류/필수값 누락)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "만료/위조/미등록 Refresh 또는 deviceId 불일치"
            )
    })
    public ApiResponse<Tokens> refresh(@Valid @RequestBody RefreshRequest req) {
        Tokens tokens = refreshTokenService.rotate(req.getRefreshToken(), req.getDeviceId());
        return ApiResponse.ok(tokens);
    }

    // 2-3. 로그아웃 (Refresh 폐기)
    @PostMapping("/logout")
    @Operation(
            summary = "2-3. 로그아웃 (Refresh 폐기)",
            description = "제출한 Refresh 토큰을 서버 측에서 폐기하여 재사용을 방지합니다.",
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "4")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LogoutRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                    { "refreshToken": "ey...", "deviceId": "A1B2C3D4" }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh")
    })
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest req) {
        refreshTokenService.revoke(req.getRefreshToken(), req.getDeviceId());
        return ApiResponse.ok();
    }

    // 2-4. 내 프로필
    @GetMapping("/me")
    @Operation(
            summary = "2-4. 내 프로필 조회",
            description = """
                로그인 사용자의 기본 정보를 조회합니다.
                - gender: ENUM { FEMALE, MAN }
                - job: ENUM { STUDENT, HOMEMAKER, OFFICE_WORKER, SOLDIER, SELF_EMPLOYED, FREELANCER, UNEMPLOYED, OTHER }
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "5"))
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ApiResponse<MyProfile> me(@AuthenticationPrincipal final UserPrincipal principal) {
        var u = authService.getMyProfile(principal.userId());
        return ApiResponse.ok(MyProfile.builder()
                .id(u.getId()).uuid(u.getUuid()).name(u.getName()).phoneNumber(u.getPhoneNumber())
                .gender(u.getGender()==null?null:u.getGender().name())
                .birthDate(u.getBirthDate().toLocalDate())
                .baseDay((int) u.getBaseDay())
                .job(u.getJob()==null?null:u.getJob().name())
                .createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt()).build());
    }

    // 2-5. PIN 재설정 요청 (SMS 발송)
    @PostMapping("/password/reset-request")
    @Operation(
            summary = "2-5. PIN 재설정 요청 (SMS 발송)",
            description = "해당 휴대폰으로 PIN 재설정용 인증코드(6자리)를 발송합니다. 내부 purpose=PIN_RESET.",
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "6")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PinResetRequest.class),
                            examples = @ExampleObject(value = """
                                { "phone": "01012345678" }
                            """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "발송 성공",
                    content = @Content(schema = @Schema(implementation = SmsSendResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    public ApiResponse<SmsSendResponse> requestPinReset(@Valid @RequestBody PinResetRequest req) {
        boolean sent = authService.requestPinReset(req.getPhone());
        return ApiResponse.ok(new SmsSendResponse(sent));
    }

    // 2-6. PIN 재설정 (코드 소비)
    @PostMapping("/password/reset")
    @Operation(
            summary = "2-6. PIN 재설정 (코드 소비)",
            description = "SMS로 받은 재설정 코드(6자리)를 검증해 새 PIN(6자리)으로 설정합니다.",
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "7")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PinResetConsumeRequest.class),
                            examples = @ExampleObject(value = """
                                { "phone": "01012345678", "resetCode": "349201", "newPin": "123456" }
                            """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패/만료 코드")
    })
    public ApiResponse<Void> resetPin(@Valid @RequestBody PinResetConsumeRequest req) {
        authService.resetPin(req.getPhone(), req.getResetCode(), req.getNewPin());
        return ApiResponse.ok();
    }

    // 2-7. PIN 변경 (로그인 상태)
    @PatchMapping("/pin")
    @Operation(
            summary = "2-7. PIN 변경 (로그인 상태)",
            description = "현재 PIN(6자리) 검증 후 새 PIN(6자리)으로 변경합니다.",
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "8")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PinChangeRequest.class),
                            examples = @ExampleObject(value = """
                                { "currentPin": "123456", "newPin": "654321" }
                            """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "현재 PIN 불일치")
    })
    public ApiResponse<Void> changePin(@AuthenticationPrincipal final UserPrincipal principal,
                                       @Valid @RequestBody PinChangeRequest req) {
        authService.changePin(principal.userId(), req.getCurrentPin(), req.getNewPin());
        return ApiResponse.ok();
    }

    // 2-8. 기기 검증(선택)
    @PostMapping("/device/verify")
    @Operation(
            summary = "2-8. 기기 검증 (선택)",
            description = """
                기기 검증 목적의 SMS 코드(6자리)를 검증합니다.
                - purpose ENUM: DEVICE_VERIFY
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "9")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceVerifyRequest.class),
                            examples = @ExampleObject(value = """
                                { "phone": "01012345678", "deviceId": "A1B2C3D4", "code": "112233" }
                            """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "검증 결과 반환",
                    content = @Content(schema = @Schema(implementation = SmsVerifyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    public ApiResponse<SmsVerifyResponse> verifyDevice(@Valid @RequestBody DeviceVerifyRequest req) {
        boolean ok = authService.verifyDevice(req.getPhone(), req.getDeviceId(), req.getCode());
        return ApiResponse.ok(new SmsVerifyResponse(ok));
    }

    // 2-9-1. SMS 발송
    @PostMapping("/sms/send")
    @Operation(
            summary = "2-9-1. SMS 인증코드 발송",
            description = """
                SMS 인증코드를 발송합니다.
                - purpose ENUM: { LOGIN, DEVICE_VERIFY, PIN_RESET, SIGNUP }
                - deviceId는 목적에 따라 필수/선택
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "10")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SmsSendRequest.class),
                            examples = {
                                    @ExampleObject(name = "LOGIN", value = """
                                    { "phone": "01012345678", "purpose": "LOGIN", "deviceId": "A1B2C3D4" }
                                """),
                                    @ExampleObject(name = "PIN_RESET", value = """
                                    { "phone": "01012345678", "purpose": "PIN_RESET" }
                                """)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "발송 성공",
                    content = @Content(schema = @Schema(implementation = SmsSendResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    public ApiResponse<SmsSendResponse> sendSms(@Valid @RequestBody SmsSendRequest req) {
        boolean sent = authService.sendSmsCode(req.getPhone(), req.getPurpose(), req.getDeviceId());
        return ApiResponse.ok(new SmsSendResponse(sent));
    }

    // 2-9-2. 회원가입 전용 sms전송
    @PostMapping("/sms/send-signup")
    @Operation(
            summary = "가입용 SMS 인증코드 발송 (purpose=SIGNUP 고정)",
            description = "회원가입 절차 전용 발송 엔드포인트입니다. 서버가 purpose를 SIGNUP으로 고정합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "10.1")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PinResetRequest.class), // phone만 필요해서 재사용
                            examples = @ExampleObject(value = """
                            { "phone": "01012345678" }
                        """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "발송 성공",
                    content = @Content(schema = @Schema(implementation = SmsSendResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    public ApiResponse<SmsSendResponse> sendSignupSms(@Valid @RequestBody PinResetRequest req) {
        boolean sent = authService.sendSmsCode(req.getPhone(), "SIGNUP", null);
        return ApiResponse.ok(new SmsSendResponse(sent));
    }


    // 2-10. SMS 검증(일반)
    @PostMapping("/sms/verify")
    @Operation(
            summary = "2-10. SMS 인증코드 검증",
            description = """
                SMS 인증코드(6자리)를 검증합니다.
                - purpose ENUM: { LOGIN, DEVICE_VERIFY, PIN_RESET, SIGNUP }
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "11")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SmsVerifyRequest.class),
                            examples = @ExampleObject(value = """
                                { "phone": "01012345678", "purpose": "LOGIN", "code": "445566" }
                            """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "검증 결과 반환",
                    content = @Content(schema = @Schema(implementation = SmsVerifyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패/만료 코드")
    })
    public ApiResponse<SmsVerifyResponse> verifySms(@Valid @RequestBody SmsVerifyRequest req) {
        boolean verified = authService.verifySmsCode(req.getPhone(), req.getPurpose(), req.getCode());
        return ApiResponse.ok(new SmsVerifyResponse(verified));
    }

    // 가입용: SMS 검증 + 티켓 발급
    @PostMapping("/sms/verify-signup")
    @Operation(
            summary = "가입용 SMS 검증(+signupTicket 발급)",
            description = """
                purpose=SIGNUP 전용으로 SMS 코드를 검증합니다. 성공 시 signupTicket을 발급합니다.
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "12")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SmsVerifyRequest.class),
                            examples = @ExampleObject(value = """
                                { "phone": "01012345678", "purpose": "SIGNUP", "code": "778899" }
                            """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "검증 성공/실패 + 티켓 발급 여부",
                    content = @Content(schema = @Schema(implementation = SmsVerifyForSignupResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패/만료 코드")
    })
    public ApiResponse<SmsVerifyForSignupResponse> verifySmsForSignup(@Valid @RequestBody SmsVerifyRequest req) {
        final String P = "SIGNUP";
        boolean ok = authService.verifySmsCode(req.getPhone(), P, req.getCode());
        if (!ok) return ApiResponse.ok(new SmsVerifyForSignupResponse(false, null));
        String ticket = authService.issueSignupTicket(req.getPhone(), P);
        return ApiResponse.ok(new SmsVerifyForSignupResponse(true, ticket));
    }

    // 2-11. 토큰 유효성 점검
    @GetMapping("/token/validate")
    @Operation(
            summary = "2-11. 토큰 유효성 점검",
            description = "Authorization 헤더의 Bearer 액세스 토큰을 검증합니다.",
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "13"))
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "검증 결과 반환",
                    content = @Content(schema = @Schema(implementation = TokenValidateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰/만료")
    })
    public ApiResponse<TokenValidateResponse> validateToken(
            @Parameter(
                    name = "Authorization",
                    required = true,
                    description = "Bearer 액세스 토큰",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String authorization
    ) {
        var r = authService.validateToken(authorization);
        return ApiResponse.ok(new TokenValidateResponse(r.valid, r.subject, r.deviceId));
    }
}
