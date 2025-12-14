package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = {"accessToken","refreshToken"})     // 로그에 토큰 노출 방지
@JsonInclude(JsonInclude.Include.NON_NULL)               // refreshToken 없으면 생략
public class Tokens {

    @Schema(description = "액세스 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String accessToken;

    @Schema(description = "리프레시 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String refreshToken;

    public Tokens(String accessToken, String refreshToken) {
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
    }

    // 편의 생성자
    public static Tokens of(String accessToken, String refreshToken) {
        return new Tokens(accessToken, refreshToken);
    }
    public static Tokens accessOnly(String accessToken) {
        return new Tokens(accessToken, null);
    }
}
