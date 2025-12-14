package com.ssafy.b108.walletslot.backend.domain.ai_report.support;

import java.time.LocalDate;
import java.time.YearMonth;

public final class BillingPeriod {
    private final LocalDate start;
    private final LocalDate end;

    private BillingPeriod(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public LocalDate start() { return start; }
    public LocalDate end() { return end; }

    // 기준일에 도달하면 전달의 기준일부터 기준일의 전날까지
    public static BillingPeriod forBaseDay(LocalDate today, int baseDay) {
        YearMonth prev = YearMonth.from(today).minusMonths(1);
        int startDay = Math.min(baseDay, prev.lengthOfMonth());
        LocalDate start = prev.atDay(startDay);
        LocalDate end   = today.minusDays(1);
        return new BillingPeriod(start, end);
    }
}