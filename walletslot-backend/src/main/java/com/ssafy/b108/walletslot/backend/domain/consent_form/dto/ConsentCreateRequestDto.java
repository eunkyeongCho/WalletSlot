package com.ssafy.b108.walletslot.backend.domain.consent_form.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class ConsentCreateRequestDto {

    @Schema(description = "동의서(폼) UUID", example = "mydata-v1")
    @NotBlank
    private String consentFormUuid;

    @Schema(description = "만료일(미전달 시 +1년)", example = "2030-12-31T23:59:59")
    private LocalDateTime expiredAt;
}
