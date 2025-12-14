package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SSAFYGetTransactionResponseDto")
public class SSAFYGetTransactionResponseDto {

    // Field
    @JsonProperty("REC")
    private Rec REC;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SSAFYGetTransactionResponseDto_Rec")
    public static class Rec {

        // Field
        private String transactionSummary;
    }
}