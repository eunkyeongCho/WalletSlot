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
@Schema(name = "GetSlotListResponseDto")
public class GetSlotListResponseDto {

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
    @Schema(name = "GetSlotListResponseDto_Data")
    public static class Data {

        // Field
        List<SlotDto> slots;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "GetSlotListResponseDto_SlotDto")
    public static class SlotDto {

        // Field
        private String SlotId;
        private String name;

        @JsonProperty("isSaving")
        private boolean isSaving;

        private Integer rank;
    }
}
