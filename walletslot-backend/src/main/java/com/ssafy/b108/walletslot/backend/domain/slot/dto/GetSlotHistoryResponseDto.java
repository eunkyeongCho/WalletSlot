package com.ssafy.b108.walletslot.backend.domain.slot.dto;

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
@Schema(name = "GetSlotHistoryResponseDto")
public class GetSlotHistoryResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetSlotHistoryResponseDto_Data")
    public static class Data {

        // Field
        private SlotDto slot;
        private List<SlotHistoryDto> history;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetSlotHistoryResponseDto_SlotDto")
    public static class SlotDto {

        // Field
        private String slotId;
        private String slotName;

        @JsonProperty("isCustom")
        private boolean isCustom;

        private String customName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetSlotHistoryResponseDto_SlotHistoryDto")
    public static class SlotHistoryDto {

        // Field
        private String slotHistoryId;
        private Long oldBudget;
        private Long newBudget;
        private LocalDateTime changedAt;
    }
}
