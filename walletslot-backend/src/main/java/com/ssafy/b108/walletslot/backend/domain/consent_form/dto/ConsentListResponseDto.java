package com.ssafy.b108.walletslot.backend.domain.consent_form.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentListResponseDto {
    private List<ConsentListItemDto> consents;
}
