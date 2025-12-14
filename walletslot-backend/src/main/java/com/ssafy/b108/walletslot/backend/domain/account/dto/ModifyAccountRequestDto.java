package com.ssafy.b108.walletslot.backend.domain.account.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyAccountRequestDto {

    // Field
    private String alias;
    private Boolean isPrimary;
}
