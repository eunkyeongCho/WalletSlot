package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class NotifyUncategorizedRequestDto {

    @NotNull
    @Schema(description = "대상 사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("targetUserId")
    private Long targetUserId;

    @Schema(description = "가맹점명", example = "스타벅스")
    @JsonProperty("merchant")
    private String merchant;

    @Schema(description = "금액(원)", example = "5800")
    @JsonProperty("amount")
    private Long amount;

    @Schema(description = "거래 요약(선택)", example = "STARBUCKS HONGDAE")
    @JsonProperty("transactionSummary")
    private String transactionSummary;

    @Schema(description = "거래 UUID(선택)", example = "tx-uuid-1234")
    @JsonProperty("transactionUuid")
    private String transactionUuid;

    @Schema(description = "거래 시각(선택, ISO-8601)", example = "2025-09-28T12:34:56")
    @JsonProperty("transactionAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionAt;
}
