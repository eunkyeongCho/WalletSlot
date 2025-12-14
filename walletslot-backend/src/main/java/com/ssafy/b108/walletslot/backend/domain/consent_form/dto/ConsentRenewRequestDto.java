package com.ssafy.b108.walletslot.backend.domain.consent_form.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class ConsentRenewRequestDto {

    @Schema(description = "이전 동의 UUID(있으면 이를 기준으로 갱신)")
    private String previousUserConsentUuid;

    @Schema(description = "이전 동의 UUID가 없을 때 필수인 폼 UUID")
    private String consentFormUuid;

    @Schema(description = "새 만료일(미전달 시 +1년)")
    private LocalDateTime expiredAt;
}
