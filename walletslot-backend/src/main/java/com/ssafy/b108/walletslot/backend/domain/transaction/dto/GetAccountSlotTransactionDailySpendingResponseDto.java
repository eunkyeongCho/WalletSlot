package com.ssafy.b108.walletslot.backend.domain.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "GetAccountSlotTransactionDailySpendingResponseDto")
public class GetAccountSlotTransactionDailySpendingResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountSlotTransactionDailySpendingResponseDto_Data")
    public static class Data {

        // Field
        private LocalDate startDate;
        private List<TransactionDto> transactions;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountSlotTransactionDailySpendingResponseDto_TransactionDto")
    public static class TransactionDto {

        // Field
        private LocalDate date;
        private Long spent;
    }
}
