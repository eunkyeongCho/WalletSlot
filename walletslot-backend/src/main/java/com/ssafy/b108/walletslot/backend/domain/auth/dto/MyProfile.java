package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyProfile {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "2f8c3c3d-....")
    private String uuid;

    @Schema(example = "김싸피")
    private String name;

    @Schema(example = "01012345678")
    private String phoneNumber;

    @Schema(
            description = "성별 ENUM",
            example = "MAN",
            allowableValues = {"FEMALE", "MAN"}
    )
    private String gender;

    @Schema(description = "yyyy-MM-dd", example = "1999-09-09")
    private LocalDate birthDate;

    @Schema(description = "1~28", example = "10")
    private Integer baseDay;

    @Schema(
            description = "직업 ENUM",
            example = "OFFICE_WORKER",
            allowableValues = {
                    "STUDENT","HOMEMAKER","OFFICE_WORKER","SOLDIER",
                    "SELF_EMPLOYED","FREELANCER","UNEMPLOYED","OTHER"
            }
    )
    private String job;

    @Schema(example = "2025-09-01T12:34:56")
    private LocalDateTime createdAt;

    @Schema(example = "2025-09-24T09:00:00")
    private LocalDateTime updatedAt;
}
