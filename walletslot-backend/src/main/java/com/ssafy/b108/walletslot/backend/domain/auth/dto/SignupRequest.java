package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SignupRequest {

    @Schema(example = "김싸피")
    @NotBlank
    String name;

    @Schema(example = "01012345678")
    @NotBlank
    String phone;

    @Schema(
            description = "성별 ENUM",
            example = "MAN",
            allowableValues = {"FEMALE", "MAN"}
    )
    @NotBlank
    String gender;

    @Schema(description = "yyyy-MM-dd", example = "1999-09-09")
    @NotBlank
    String birthDate;

    @Schema(description = "SIGNUP 검증 후 발급된 티켓", example = "3af3-....")
    @NotBlank
    String signupTicket;

    @Schema(description = "로그인용 PIN(6자리 숫자)", example = "123456")
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN은 숫자 6자리여야 합니다.")
    String pin;

    @Schema(description = "1~28 사이 권장", example = "10")
    @Min(1) @Max(28)
    Integer baseDay;

    @Schema(
            description = "직업 ENUM",
            example = "OFFICE_WORKER",
            allowableValues = {
                    "STUDENT","HOMEMAKER","OFFICE_WORKER","SOLDIER",
                    "SELF_EMPLOYED","FREELANCER","UNEMPLOYED","OTHER"
            }
    )
    String job;

    @Schema(example = "A1B2C3D4")
    @NotBlank
    String deviceId;

    @Schema(
            description = "플랫폼 ENUM",
            example = "ANDROID",
            allowableValues = {"ANDROID","IOS"} // 필요 시 WEB 추가
    )
    @NotBlank
    String platform;

    @Schema(description = "FCM 토큰 등", example = "fcm-xxx")
    String pushToken;

    @Schema(description = "푸시 허용 여부", example = "true", defaultValue = "true")
    Boolean pushEnabled;
}
