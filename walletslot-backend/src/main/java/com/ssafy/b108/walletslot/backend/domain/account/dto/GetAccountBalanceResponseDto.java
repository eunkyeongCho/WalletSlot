package com.ssafy.b108.walletslot.backend.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "GetBalanceResponseDto")
public class GetAccountBalanceResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetBalanceResponseDto_Data")
    public static class Data {

        // Field
        private String accountId;
        private Long balance;
    }
}
