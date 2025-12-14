package com.ssafy.b108.walletslot.backend.domain.consent_form.dto;

import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRenewResponseDto {
    private String newUserConsentUuid;
    private String consentFormUuid;
    private UserConsent.Status status;
    private LocalDateTime agreedAt;
    private LocalDateTime expiredAt;
}
