package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SSAFYRequestVerificationResponseDto {

    // Field
    @JsonProperty("REC")
    private Rec REC;

    // Nested Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Rec {

        // Field
        private String transactionUniqueNo;
        private String accountNo;
    }
}
