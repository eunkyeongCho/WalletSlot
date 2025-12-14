package com.ssafy.b108.walletslot.backend.domain.ocr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class ReceiptOcrResponseDto {

    @Schema(description = "상호명")
    private String storeName;

    @Schema(description = "결제일 (yyyy-MM-dd)")
    private String date;

    @Schema(description = "결제시각 (HH:mm:ss)")
    private String time;

    @Schema(description = "항목 목록(이름/수량/가격)")
    private List<ReceiptOcrItemDto> items;
}
