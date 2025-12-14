package com.ssafy.b108.walletslot.backend.domain.user.dto;

import com.ssafy.b108.walletslot.backend.domain.user.entity.User;

import java.time.format.DateTimeFormatter;

public final class UserDtoAssembler {

    private static final DateTimeFormatter D = DateTimeFormatter.ISO_LOCAL_DATE;

    private UserDtoAssembler() { }

    /** 기본 메시지 "OK" */
    public static MeResponseDto toMeResponse(User u, String currentEmail) {
        return toMeResponse(u, currentEmail, "OK");
    }

    /** 메시지 지정 가능 */
    public static MeResponseDto toMeResponse(User u, String currentEmail, String message) {
        return MeResponseDto.builder()
                .success(true)
                .message(message)
                .data(toMeData(u, currentEmail))
                .build();
    }

    /** 내부: Data만 조립 */
    public static MeResponseDto.Data toMeData(User u, String currentEmail) {
        return MeResponseDto.Data.builder()
                .id(u.getId())
                .uuid(u.getUuid())
                .name(u.getName())
                .phoneNumber(u.getPhoneNumber())
                .email(currentEmail) // email 테이블에서 결정된 현재 이메일
                .gender(u.getGender() != null ? u.getGender().name() : null)
                .birthDate(u.getBirthDate() != null ? u.getBirthDate().toLocalDate().format(D) : null)
                .baseDay(u.getBaseDay() != null ? u.getBaseDay().intValue() : null)
                .job(u.getJob() != null ? u.getJob().name() : null)
                .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                .updatedAt(u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null)
                .build();
    }
}
