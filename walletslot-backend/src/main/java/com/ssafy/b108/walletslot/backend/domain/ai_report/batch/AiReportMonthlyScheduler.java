package com.ssafy.b108.walletslot.backend.domain.ai_report.batch;

import com.ssafy.b108.walletslot.backend.domain.ai_report.service.AiReportBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportMonthlyScheduler {

    private final AiReportBatchService batchService;

    @Value("${scheduling.zone:Asia/Seoul}")
    private String schedulingZone;

    @Scheduled(cron = "${scheduling.ai-report.cron:0 10 0 * * *}", zone = "${scheduling.zone:Asia/Seoul}")
    public void run() {
        LocalDate today = LocalDate.now(ZoneId.of(schedulingZone)); // ← yml과 동일한 타임존
        log.info("[AI-REPORT][SCHED] start for {}", today);
        batchService.generateForAllUsersIfDue(today);
        log.info("[AI-REPORT][SCHED] done for {}", today);
    }
}