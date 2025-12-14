package com.ssafy.b108.walletslot.backend.domain.slot.dto;

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
@Schema(name = "RecommendSlotsByProfileResponseDto")
public class RecommendSlotsByProfileResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "RecommendSlotsByProfileResponseDto_Data")
    public static class Data {

        // Field
        private BankDto bank;
        private AccountDto account;
        private List<SlotDto> recommededSlots;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "RecommendSlotsByProfileResponseDto_BankDto")
    public static class BankDto {

        // Field
        private String bankId;
        private String name;
        private String color;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "RecommendSlotsByProfileResponseDto_AccountDto")
    public static class AccountDto {

        // Field
        private String accountId;
        private String accountNo;
        private Long accountBalance;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "RecommendSlotsByProfileResponseDto_SlotDto")
    public static class SlotDto {

        // Field
        private String slotId;
        private String name;
        private Long initialBudget;
    }
}
