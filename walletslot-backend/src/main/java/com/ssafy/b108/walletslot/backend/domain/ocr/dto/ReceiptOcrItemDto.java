package com.ssafy.b108.walletslot.backend.domain.ocr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class ReceiptOcrItemDto {

    @Schema(description = "품목명")
    private String name;

    @Schema(description = "수량")
    private Integer quantity;

    @Schema(description = "가격(원)")
    private Integer price;
}
