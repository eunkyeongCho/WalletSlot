package com.ssafy.b108.walletslot.backend.domain.ai_report.service;

import java.time.LocalDate;

public interface AiReportBatchService {
    void generateForAllUsersIfDue(LocalDate today);
}