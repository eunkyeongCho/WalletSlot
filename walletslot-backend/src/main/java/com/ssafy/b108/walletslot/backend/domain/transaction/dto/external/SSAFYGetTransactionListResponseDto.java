package com.ssafy.b108.walletslot.backend.domain.transaction.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SSAFYGetTransactionListResponseDto {

    // Field
    @JsonProperty("REC")
    private Rec REC;

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
        private Long transactionUniqueNo;
        private String transactionDate;
        private String transactionTime;
        private String transactionTypeName;
        private String transactionAccountNo;    // 이체타입 거래인 경우 상대방의 계좌번호
        private Long transactionBalance;    // 거래금액
        private Long transactionAfterBalance;    // 거래 후 잔액
        private String transactionSummary;
    }
}
