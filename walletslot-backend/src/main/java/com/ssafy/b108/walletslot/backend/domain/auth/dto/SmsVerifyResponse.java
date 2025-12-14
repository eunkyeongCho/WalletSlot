package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerifyResponse {
    private boolean verified;
}
