package com.ssafy.b108.walletslot.backend.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "VerifyAccountResponseDto")
public class VerifyAccountResponseDto {

    // Field
    private boolean success;
    private String message;
    private Data data;

    // Nested Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "VerifyAccountResponseDto_Data")
    public static class Data {

        // Field
        private String accountNo;
    }
}
