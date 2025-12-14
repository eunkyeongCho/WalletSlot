package com.ssafy.b108.walletslot.backend.domain.consent_form.dto;

import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentCreateResponseDto {

    private String userConsentUuid;

    private String consentFormUuid;

    @Schema(description = "동의서 제목")
    private String consentFormTitle;

    private UserConsent.Status status;

    private LocalDateTime agreedAt;

    private LocalDateTime expiredAt;
}
