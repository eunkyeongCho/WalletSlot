package com.ssafy.b108.walletslot.backend.domain.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ModifyTransactionReqeustDto")
public class ModifyTransactionReqeustDto {

    // Field
    private String accountSlotId;
}
