package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SSAFYGetAccountHolderNameResponseDto {

    // Field
    @JsonProperty("REC")
    private Rec REC;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    // Nested Class
    public static class Rec {

        // Field
        private String userName;
    }
}
