package com.ssafy.b108.walletslot.backend.domain.ai_report.dto;

import lombok.*;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeleteAiReportResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Data {
        private String reportId; // UUID
    }
}
