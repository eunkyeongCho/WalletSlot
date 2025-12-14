package com.ssafy.b108.walletslot.backend.config.web.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class YearMonthConverter implements Converter<String, YearMonth> {
    private static final DateTimeFormatter[] CANDIDATES = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("yyyyMM"),
            DateTimeFormatter.ofPattern("yyyy/MM"),
            DateTimeFormatter.ofPattern("MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/yyyy")
    };

    @Override
    public YearMonth convert(String source) {
        if (source == null) return null;
        String s = source.trim();
        if (s.isEmpty()) return null;

        for (DateTimeFormatter fmt : CANDIDATES) {
            try { return YearMonth.parse(s, fmt); } catch (Exception ignored) {}
        }
        if (s.matches("\\d{4}")) { // 연도만 온 경우
            int year = Integer.parseInt(s);
            return YearMonth.of(year, YearMonth.now().getMonth()); // 항상 1월이면 1로 바꾸세요
        }
        throw new IllegalArgumentException("ym 형식 오류: 허용 예 'yyyy-MM', 'yyyy', 'yyyyMM', 'yyyy/MM', 'MM-yyyy', 'MM/yyyy'");
    }
}
