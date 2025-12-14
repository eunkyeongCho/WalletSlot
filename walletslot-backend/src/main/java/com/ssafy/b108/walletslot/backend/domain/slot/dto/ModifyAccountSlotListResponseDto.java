package com.ssafy.b108.walletslot.backend.domain.slot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(name = "ModifyAccountSlotListResponseDto")
public class ModifyAccountSlotListResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ModifyAccountSlotListResponseDto_Data")
    public static class Data {

        // Field
        private List<SlotDto> slots;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ModifyAccountSlotListResponseDto_SlotDto")
    public static class SlotDto {

        // Field
        private String accountSlotId;
        private String name;

        @JsonProperty("isSaving")
        private boolean isSaving;

        @JsonProperty("isCustom")
        private boolean isCustom;

        private String customName;
        private Long initialBudget;
    }
}
