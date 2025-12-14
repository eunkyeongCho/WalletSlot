package com.ssafy.b108.walletslot.backend.domain.account.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestVerificationRequestDto {

    // Field
    private String userName;
    private String bankId;
    private String accountNo;
}
