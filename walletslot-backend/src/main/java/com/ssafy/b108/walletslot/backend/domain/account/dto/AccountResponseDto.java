package com.ssafy.b108.walletslot.backend.domain.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponseDto {

    // Field
    private String accountId;
    private String bankId;
    private String bankName;
    private String accountNo;
    private String alias;
    private String accountBalance;
}
