package com.ssafy.b108.walletslot.backend.domain.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AddSplitTransactionsResponseDto")
public class AddSplitTransactionsResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSplitTransactionsResponseDto_Data")
    public static class Data {

        // Field
        private OriginalTransactionDto originalTransaction;
        private List<SplitTransactionDto> splitTransactions;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSplitTransactionsResponseDto_OriginalTransactionDto")
    public static class OriginalTransactionDto {

        // Field
        private String transactionId;
        private String type;
        private String opponentAccountNo;
        private String summary;
        private Long amount;
        private Long balance;
        private LocalDateTime transactionAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSplitTransactionsResponseDto_SlotDto")
    public static class SlotDto {

        // Field
        private String accountSlotId;
        private String name;

        @JsonProperty("isSaving")
        private boolean isSaving;

        @JsonProperty("isCustom")
        private boolean isCustom;

        private String customName;
        private Long currentBudget;
        private Long spent;
        private Long remainingBudget;

        @JsonProperty("isBudgetExceeded")
        private boolean isBudgetExceeded;

        private Long exceededBudget;

        // Method
        @JsonProperty("isSaving")
        public boolean isSaving() {
            return isSaving;
        }

        @JsonProperty("isSaving")
        public void setSaving(boolean saving) {
            this.isSaving = saving;
        }

        @JsonProperty("isCustom")
        public boolean isCustom() {
            return isCustom;
        }

        @JsonProperty("isCustom")
        public void setCustom(boolean custom) {
            this.isCustom = custom;
        }

        @JsonProperty("isBudgetExceeded")
        public boolean isBudgetExceeded() {
            return isBudgetExceeded;
        }

        @JsonProperty("isBudgetExceeded")
        public void setBudgetExceeded(boolean budgetExceeded) {
            this.isBudgetExceeded = budgetExceeded;
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSplitTransactionsResponseDto_TransactionDto")
    public static class TransactionDto {

        // Field
        private String transactionId;
        private Long amount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSplitTransactionsResponseDto_SplitTransactionDto")
    public static class SplitTransactionDto {

        // Field
        private SlotDto slot;
        private TransactionDto transaction;
    }
}
