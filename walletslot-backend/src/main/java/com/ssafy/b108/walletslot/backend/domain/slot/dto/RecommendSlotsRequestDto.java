package com.ssafy.b108.walletslot.backend.domain.slot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendSlotsRequestDto {

    // Field
    String startDate;
    String endDate;
}
