package com.ssafy.b108.walletslot.backend.domain.user.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MeBaseDayResponseDto {
    boolean success;
    String message;
    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {
        Integer baseDay; // 1~31 또는 null
    }
}
