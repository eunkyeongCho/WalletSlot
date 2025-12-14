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
public class AddSlotListRequestDto {

    // Field
    private List<SlotDto> slots;

    // Nested Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotDto {

        // Field
        private String slotId;

        @JsonProperty("isCustom")
        private boolean isCustom;

        private String customName;
        private Long initialBudget;
    }
}
