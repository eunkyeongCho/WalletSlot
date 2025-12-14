package com.ssafy.b108.walletslot.backend.domain.ai_report.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class GetAiReportArchiveResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Data {
        private String yearMonth;            // 현재 화면 월 (YYYY-MM)
        private String prevYearMonth;        // 좌(이전 월)
        private String nextYearMonth;        // 우(다음 월)
        private List<String> yearMonths;     // 전체 월 목록 (최근→과거)
        private List<Item> reports;          // 해당 월 레포트들 (최신→과거)
    }

    // 각 아이템이 7-1 응답의 data와 동일한 필드 세트
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        private String reportId;                 // ai_report.uuid
        private LocalDateTime createdAt;         // ai_report.created_at

        private GetAiReportResponseDto.Period period;
        private GetAiReportResponseDto.Summary summary;
        private List<GetAiReportResponseDto.SlotRow> slots;
        private GetAiReportResponseDto.Redistribution redistribution;
        private GetAiReportResponseDto.Insights insights;
        private GetAiReportResponseDto.PersistInfo persist; // id=uuid, createdAt=created_at
    }
}
