package com.ssafy.b108.walletslot.backend.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class LocalDateTimeFormatter {

    // Method
    public static Map<String, String> formatter() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

        return Map.of(
                "date", now.format(dateFormatter),
                "time", now.format(timeFormatter)
        );
    }

    /**
     * yyyyMMddHHmmss을 LocalDateTime으로 파싱
     */
    public static LocalDateTime StringToLocalDateTime(String date, String time) {

        // 문자열 조립
        String dateTimeStr = date + time;

        // 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // LocalDateTime 파싱
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);

        return localDateTime;
    }

    /**
     * 개월 수를 받아서 해당 개월 수 만큼 전의 날짜를 반환해주는 메서드
     */
    public static Map<String, String> fomatterWithMonthsAgo(Short period) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime ago = now.minusMonths(period);

        return Map.of(
                "date", now.format(dateFormatter),
                "dateMonthsAgo", ago.format(dateFormatter)
        );
    }

    /**
     * 생일을 LocalDateTime으로 받아서 나이계산을 해주는 메서드
     */
    public static int calculateAge(LocalDateTime birthDateTime) {
        // 현재 일자 (서울 기준)
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // LocalDateTime → LocalDate (시간은 무시)
        LocalDate birthDate = birthDateTime.toLocalDate();

        // Period를 이용해 두 날짜 차이 계산
        return Period.between(birthDate, today).getYears();
    }

    /**
     * yyyyMMdd 형식 문자열을 LocalDate 타입으로 파싱해주는 메서드
     */
    public static LocalDate stringToLocalDate(String dateStr) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(dateStr, formatter);

        return localDate;
    }
}
