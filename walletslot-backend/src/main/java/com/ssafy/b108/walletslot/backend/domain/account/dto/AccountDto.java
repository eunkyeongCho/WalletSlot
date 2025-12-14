package com.ssafy.b108.walletslot.backend.domain.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto {

    // Field
    private String accountId;
    private String bankCode;
    private String bankName;
    private String accountNo;
    private String alias;
    private String accountBalance;
}