package com.ssafy.b108.walletslot.backend.domain.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "GetAccountTransactionListResponseDto")
public class GetAccountTransactionListResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountTransactionListResponseDto_Data")
    public static class Data {

        // Field
        private List<TransactionDto> transactions;
        private Boolean hasNext;
        private LocalDateTime nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountTransactionListResponseDto_TransactionDto")
    public static class TransactionDto {

        // Field
        private String transactionId;
        private String type;
        private String opponentAccountNo;
        private String summary;
        private Long amount;
        private Long balance;
        private LocalDateTime transactionAt;
    }
}
