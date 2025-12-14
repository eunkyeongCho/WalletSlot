package com.ssafy.b108.walletslot.backend.domain.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AddSplitTransactionsRequestDto")
public class AddSplitTransactionsRequestDto {

    // Field
    List<SplitTransactionDto> transactions;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSplitTransactionsRequestDto_SplitTransactionDto")
    public static class SplitTransactionDto {

        // Field
        private String accountSlotId;
        private Long amount;
    }
}
