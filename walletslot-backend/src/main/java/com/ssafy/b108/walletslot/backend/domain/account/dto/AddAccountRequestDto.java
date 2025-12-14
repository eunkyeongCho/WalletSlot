package com.ssafy.b108.walletslot.backend.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AddAccountRequestDto")
public class AddAccountRequestDto {

    // Field
    private List<AccountDto> accounts;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AddAccountRequestDto_AccountDto")
    // Nested Class
    public static class AccountDto {

        // Field
        private String bankId;
        private String accountNo;
    }
}
