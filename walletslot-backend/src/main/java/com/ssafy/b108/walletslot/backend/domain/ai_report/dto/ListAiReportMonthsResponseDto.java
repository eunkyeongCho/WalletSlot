package com.ssafy.b108.walletslot.backend.domain.ai_report.dto;

import lombok.*;

import java.util.List;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ListAiReportMonthsResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Data {
        private List<String> yearMonths; // ["2025-09","2025-08",...]
    }
}
