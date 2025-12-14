package com.ssafy.b108.walletslot.backend.domain.slot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AddSlotListResponseDto")
public class AddSlotListResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSlotListResponseDto_Data")
    public static class Data {

        // Field
        List<SlotDto> slots;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddSlotListResponseDto_SlotDto")
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
