package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.b108.walletslot.backend.domain.account.dto.AccountDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SSAFYGetAccountsResponseDto")
public class SSAFYGetAccountsResponseDto {

    // Field
    @JsonProperty("REC")
    private List<AccountDto> REC;
}
