package com.ssafy.b108.walletslot.backend.domain.account.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyAccountRequestDto {

    // Field
    private String userName;
    private String accountNo;
    private String authIdentifier;
}
