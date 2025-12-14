package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SmsVerifyForSignupResponse {
    private boolean verified;
    @Schema(description = "최종 가입 요청에 동봉할 티켓") private String signupTicket;
}
