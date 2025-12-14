package com.ssafy.b108.walletslot.backend.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountsResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountListResponseDto_Data")
    public static class Data {

        // Field
        private List<AccountResponseDto> accounts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountListResponseDto_AccountResponseDto")
    public static class AccountResponseDto {

        // Field
        private String bankId;
        private String bankName;
        private String accountNo;
        private String accountBalance;
    }
}
