package com.ssafy.b108.walletslot.backend.domain.auth.controller;

import com.ssafy.b108.walletslot.backend.domain.auth.dto.SignupRequest;
import com.ssafy.b108.walletslot.backend.domain.auth.dto.SignupResponse;
import com.ssafy.b108.walletslot.backend.domain.auth.service.SignupService;
import com.ssafy.b108.walletslot.backend.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Signup", description = "회원가입 관련 API")
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입(원샷)",
            description = """
                문자인증 완료 후 발급받은 signupTicket과 모든 가입정보를 한 번에 제출합니다.

                필드 규칙
                - phone: 하이픈 없는 숫자(예: 01012345678)
                - pin: 숫자 6자리(예: 123456)
                - gender: { FEMALE, MAN }
                - job: { STUDENT, HOMEMAKER, OFFICE_WORKER, SOLDIER, SELF_EMPLOYED, FREELANCER, UNEMPLOYED, OTHER }
                - platform: { ANDROID, IOS }  // (필요 시 WEB 확장 가능)
                - baseDay: 1~28 권장
                - pushEnabled: true/false

                처리 흐름(요약)
                1) signupTicket 유효성 검증 (purpose=SIGNUP)
                2) 사용자 생성(Bcrypt PIN 해시 저장) + 기본 환경(device, push) 등록
                3) Access/Refresh 동시 발급 및 반환
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "10")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "모바일 전체 입력",
                                            value = """
                                            {
                                              "name": "김싸피",
                                              "phone": "01012345678",
                                              "gender": "MAN",
                                              "birthDate": "1999-09-09",
                                              "signupTicket": "3af3-xxxx-xxxx",
                                              "pin": "123456",
                                              "baseDay": 10,
                                              "job": "OFFICE_WORKER",
                                              "deviceId": "A1B2C3D4",
                                              "platform": "ANDROID",
                                              "pushToken": "fcm-xxx",
                                              "pushEnabled": true
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "최소 필수(디바이스/푸시는 추후 등록)",
                                            value = """
                                            {
                                              "name": "홍길동",
                                              "phone": "01098765432",
                                              "gender": "FEMALE",
                                              "birthDate": "2000-01-01",
                                              "signupTicket": "a1b2-xxxx-xxxx",
                                              "pin": "123456",
                                              "baseDay": 15,
                                              "job": "STUDENT"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "가입 성공",
                    content = @Content(
                            schema = @Schema(implementation = SignupResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                    {
                      "success": true,
                      "data": {
                        "userId": 1,
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                      },
                      "message": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패(잘못된 형식/만료된 티켓 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "PIN 형식 오류",
                                            value = """
                        { "success": false, "data": null, "message": "PIN은 숫자 6자리여야 합니다." }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "티켓 유효성 실패",
                                            value = """
                        { "success": false, "data": null, "message": "signupTicket이 유효하지 않거나 만료되었습니다." }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복(이미 가입된 전화번호 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "전화번호 중복",
                                    value = """
                    { "success": false, "data": null, "message": "이미 가입된 전화번호입니다." }
                    """
                            )
                    )
            )
    })
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ApiResponse.ok(signupService.signup(req));
    }
}
