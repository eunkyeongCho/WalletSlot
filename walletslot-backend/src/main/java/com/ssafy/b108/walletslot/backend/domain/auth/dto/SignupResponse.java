package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponse {
    private Long userId;
    private String accessToken;
    private String refreshToken; // refresh 비활성화면 null
}
