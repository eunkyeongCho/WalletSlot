package com.ssafy.b108.walletslot.backend.domain.slot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RemoveAccountSlotResponseDto")
public class RemoveAccountSlotResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "RemoveAccountSlotResponseDto_Data")
    public static class Data {

        // Field
        private String accountSlotId;
    }
}
