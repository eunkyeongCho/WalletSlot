package com.ssafy.b108.walletslot.backend.domain.slot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyAccountSlotRequestDto {

    // Field
    private String customName;
    private Long newBudget;
}
