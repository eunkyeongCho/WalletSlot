package com.ssafy.b108.walletslot.backend.domain.user.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MeResponseDto {
    boolean success;
    String  message;
    Data    data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {
        Long   id;
        String uuid;
        String name;
        String phoneNumber;
        String email;
        String gender;          // "FEMALE" | "MAN"
        String birthDate;       // "yyyy-MM-dd"
        Integer baseDay;
        String job;
        Long   monthlyIncome;
        String avatarUrl;
        String createdAt;       // ISO-8601
        String updatedAt;       // ISO-8601
    }
}
