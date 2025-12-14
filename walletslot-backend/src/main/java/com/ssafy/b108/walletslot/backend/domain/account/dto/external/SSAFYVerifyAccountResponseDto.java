package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SSAFYVerifyAccountResponseDto")
public class SSAFYVerifyAccountResponseDto {

    // Field
    @JsonProperty("REC")
    private Rec REC;

    // Nested Class
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SSAFYVerifyAccountResponseDto_Rec")
    public static class Rec {

        // Field
        public String status;
        public String accountNo;
    }
}
