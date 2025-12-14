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
@Schema(name = "AddDutchPayTransactionsResponseDto")
public class AddDutchPayTransactionsResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddDutchPayTransactionsResponseDto_Data")
    public static class Data {

        // Field
        private SlotAndTransactionDto originalTransaction;
        private List<SlotAndTransactionDto> dutchPayTransactions;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddDutchPayTransactionsResponseDto_SlotAndTransactionDto")
    public static class SlotAndTransactionDto {

        // Field
        private SlotDto slot;
        private TransactionDto transaction;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddDutchPayTransactionsResponseDto_SlotDto")
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
    @Schema(name = "AddDutchPayTransactionsResponseDto_TransactionDto")
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
