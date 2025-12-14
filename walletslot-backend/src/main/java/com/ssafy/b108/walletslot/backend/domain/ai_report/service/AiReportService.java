package com.ssafy.b108.walletslot.backend.domain.ai_report.service;

import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.DeleteAiReportResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.GetAiReportArchiveResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.GetAiReportResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.ListAiReportMonthsResponseDto;

import java.time.LocalDate;

public interface AiReportService {

    // 기존 시그니처(배치 등 하위호환) → 내부적으로 notify=false로 위임
    default GetAiReportResponseDto getReportByPeriod(long userId,
                                                     String accountId,   // UUID
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     boolean persist) {
        return getReportByPeriod(userId, accountId, startDate, endDate, persist, false);
    }

    // 신규: 직접 생성(시연) 등에서 알림 제어를 위해 notify 추가
    GetAiReportResponseDto getReportByPeriod(long userId,
                                             String accountId,   // UUID
                                             LocalDate startDate,
                                             LocalDate endDate,
                                             boolean persist,
                                             boolean notify);

    DeleteAiReportResponseDto delete(long userId, String accountId, String reportId); // UUID

    // ▼ 아카이브/월 목록
    ListAiReportMonthsResponseDto listMonths(long userId, String accountId);

    GetAiReportArchiveResponseDto getArchiveByMonthOrOffset(long userId,
                                                            String accountId,
                                                            String yearMonth,
                                                            Integer offset);
}
