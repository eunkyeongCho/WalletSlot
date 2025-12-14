package com.ssafy.b108.walletslot.backend.domain.slot.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotDto {

    // Field
    private String name;

    @JsonProperty("isSaving")
    private boolean isSaving;
}
