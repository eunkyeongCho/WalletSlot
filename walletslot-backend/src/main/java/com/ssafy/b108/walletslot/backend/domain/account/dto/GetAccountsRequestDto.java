package com.ssafy.b108.walletslot.backend.domain.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "GetAccountsRequestDto")
public class GetAccountsRequestDto {

    // Field
    List<BankDto> banks;

    // Nested Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "GetAccountsRequestDto_BankDto")
    public static class BankDto {

        // Field
        private String bankId;
    }
}
