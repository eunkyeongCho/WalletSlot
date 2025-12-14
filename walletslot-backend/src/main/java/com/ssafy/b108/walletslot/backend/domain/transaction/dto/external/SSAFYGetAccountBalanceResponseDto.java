package com.ssafy.b108.walletslot.backend.domain.transaction.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SSAFYGetAccountBalanceResponseDto {

    // Field
    @JsonProperty("REC")
    private Rec REC;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    // Nested Class
    public static class Rec {

        // Field
        private Long accountBalance;
    }
}
