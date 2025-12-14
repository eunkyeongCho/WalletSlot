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
@Schema(name = "RecommendSlotsByProfileRequestDto")
public class RecommendSlotsByProfileRequestDto {

    //Field
    private Boolean useAge;
    private Long income;
    private Boolean useGender;
}