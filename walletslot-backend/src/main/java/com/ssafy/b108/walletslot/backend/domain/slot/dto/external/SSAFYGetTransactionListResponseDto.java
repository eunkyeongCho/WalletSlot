package com.ssafy.b108.walletslot.backend.domain.slot.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SSAFYGetTransactionListResponseDto {

    // Field
    @JsonProperty("REC")
    private SSAFYGetTransactionListResponseDto.Rec REC;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    // Nested Class
    public static class Rec {

        // Field
        private String totalCount;
        private List<Transaction> list;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Transaction {

        // Field
        private String transactionDate;
        private String transactionTime;
        private String transactionBalance;
        private String transactionSummary;
    }
}
