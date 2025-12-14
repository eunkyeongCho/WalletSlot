package com.ssafy.b108.walletslot.backend.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AddAccountResponseDto")
public class AddAccountResponseDto {

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
    @Schema(name = "AddAccountResponseDto_Data")
    public static class Data {

        // Field
        List<AccountDto> accounts;
    }
}
