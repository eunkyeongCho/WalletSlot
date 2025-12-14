package com.ssafy.b108.walletslot.backend.domain.consent_form.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class ConsentRevokeRequestDto {
    @NotBlank
    private String userConsentUuid;
}
