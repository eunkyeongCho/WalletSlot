package com.ssafy.b108.walletslot.backend.domain.slot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "GetAccountSlotListResponseDto")
public class GetAccountSlotListResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountSlotListResponseDto_Data")
    public static class Data {

        // Field
        private List<SlotDto> slots;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetAccountSlotListResponseDto_SlotDto")
    public static class SlotDto {

        // Field
        private String slotId;
        private String name;

        @JsonProperty("isSaving")
        private boolean isSaving;

        private String accountSlotId;

        @JsonProperty("isCustom")
        private boolean isCustom;

        private String customName;
        private Long initialBudget;
        private Long currentBudget;
        private Long spent;
        private Long remainingBudget;

        @JsonProperty("isBudgetExceeded")
        private boolean isBudgetExceeded;

        private Long exceededBudget;
        private Integer budgetChangeCount;

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
}
