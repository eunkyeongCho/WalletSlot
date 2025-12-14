package com.ssafy.b108.walletslot.backend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Value
@Builder
@Jacksonized
public class MePatchRequestDto {

    @Size(min = 1, max = 64)
    String name;

    LocalDate birthDate;          // yyyy-MM-dd

    String gender;                // FEMALE | MAN (enum name)

    @Pattern(regexp = "^[+]?\\d{8,15}$")
    String phoneNumber;

    @Email
    String email;

    String job;                   // enum name

    @Min(1) @Max(28)
    Integer baseDay;

    @PositiveOrZero
    Long monthlyIncome;

    // 연락처 변경 시 필요한 단기 토큰
    String phoneVerificationToken;
    String emailVerificationToken;
}
