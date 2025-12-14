package com.ssafy.b108.walletslot.backend.domain.account.dto;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountResponseDto {

    // Field
    private boolean success;
    private String message;
    private AccountResponseDto data;
}
