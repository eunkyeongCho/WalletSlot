package com.ssafy.b108.walletslot.backend.domain.ocr.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.b108.walletslot.backend.domain.ocr.dto.ReceiptOcrItemDto;
import com.ssafy.b108.walletslot.backend.domain.ocr.dto.ReceiptOcrResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ReceiptOcrMapper {

    private final ObjectMapper om;

    public ReceiptOcrResponseDto map(String rawJson) {
        try {
            JsonNode root = om.readTree(rawJson);

            // images[0]
            JsonNode firstImage = (root.path("images").isArray() && root.path("images").size() > 0)
                    ? root.path("images").get(0) : om.createObjectNode();

            JsonNode result = firstImage.path("receipt").path("result");

            // 1) 상호명: formatted.value 우선, 없으면 text
            String storeName = pickText(
                    result.path("storeInfo").path("name").path("formatted").path("value"),
                    result.path("storeInfo").path("name").path("text")
            );

            // 2) 결제일
            String date = extractDate(result);

            // 3) 결제시각
            String time = extractTime(result);

            // 4) 품목들
            List<ReceiptOcrItemDto> items = extractItems(result);

            return ReceiptOcrResponseDto.builder()
                    .storeName(nullToEmpty(storeName))
                    .date(nullToEmpty(date))
                    .time(nullToEmpty(time))
                    .items(items)
                    .build();

        } catch (Exception e) {
            // 파싱 실패 시 빈 객체 반환(필요하면 예외로 올려도 됨)
            return ReceiptOcrResponseDto.builder()
                    .storeName("")
                    .date("")
                    .time("")
                    .items(List.of())
                    .build();
        }
    }

    /* ---------- 상세 추출 로직들 ---------- */

    private String extractDate(JsonNode result) {
        JsonNode dateNode = result.path("paymentInfo").path("date");
        JsonNode fmt = dateNode.path("formatted");

        if (hasAnyNonNull(fmt, "year", "month", "day")) {
            String y = textOr(fmt.path("year"), "");
            String m = pad2(textOr(fmt.path("month"), ""));
            String d = pad2(textOr(fmt.path("day"), ""));
            if (!y.isBlank() && !m.isBlank() && !d.isBlank()) {
                return String.format("%s-%s-%s", y, m, d);
            }
        }
        // fallback: text (그대로 혹은 후처리)
        return textOr(dateNode.path("text"), "");
    }

    private String extractTime(JsonNode result) {
        JsonNode timeNode = result.path("paymentInfo").path("time");
        JsonNode fmt = timeNode.path("formatted");

        if (hasAnyNonNull(fmt, "hour", "minute", "second")) {
            String h = pad2(textOr(fmt.path("hour"), ""));
            String m = pad2(textOr(fmt.path("minute"), ""));
            String s = pad2(textOr(fmt.path("second"), ""));
            if (!h.isBlank() && !m.isBlank() && !s.isBlank()) {
                return String.format("%s:%s:%s", h, m, s);
            }
        }
        // fallback: text → 공백 제거 등 최소 정리
        return sanitizeTime(textOr(timeNode.path("text"), ""));
    }

    private List<ReceiptOcrItemDto> extractItems(JsonNode result) {
        List<ReceiptOcrItemDto> items = new ArrayList<>();

        JsonNode subResults = result.path("subResults");
        if (!subResults.isArray()) return items;

        for (JsonNode sub : subResults) {
            JsonNode arr = sub.path("items");
            if (!arr.isArray()) continue;

            for (JsonNode it : arr) {
                // 이름: formatted.value 우선
                String name = pickText(
                        it.path("name").path("formatted").path("value"),
                        it.path("name").path("text")
                );

                // 수량: formatted.value → text → 1
                String qStr = pickText(
                        it.path("count").path("formatted").path("value"),
                        it.path("count").path("text")
                );
                Integer qty = parseIntOrDefault(qStr, 1);

                // 가격: price.price.formatted.value → price.price.text → 0
                String pStr = pickText(
                        it.path("price").path("price").path("formatted").path("value"),
                        it.path("price").path("price").path("text")
                );
                Integer price = parseAmount(pStr);

                // 빈 이름/0원 전부 무시하고 싶다면 아래 조건 추가 가능
                items.add(ReceiptOcrItemDto.builder()
                        .name(nullToEmpty(name))
                        .quantity(qty)
                        .price(price)
                        .build());
            }
        }
        return items;
    }

    /* ---------- 유틸 ---------- */

    private boolean hasAnyNonNull(JsonNode node, String... fields) {
        if (node == null || node.isMissingNode() || node.isNull()) return false;
        for (String f : fields) {
            if (node.hasNonNull(f) && !node.path(f).asText("").isBlank()) return true;
        }
        return false;
    }

    private String pickText(JsonNode preferred, JsonNode fallback) {
        String p = textOr(preferred, null);
        if (p != null && !p.isBlank()) return p;
        return textOr(fallback, "");
    }

    private String textOr(JsonNode node, String def) {
        if (node == null || node.isMissingNode() || node.isNull()) return def;
        String s = node.asText(null);
        return (s == null || s.isBlank()) ? def : s;
    }

    private String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    private String pad2(String s) {
        if (s == null) return "";
        String digits = s.replaceAll("\\D", "");
        if (digits.isEmpty()) return "";
        return (digits.length() == 1) ? "0" + digits : digits;
    }

    private final Pattern TIME_CLEANER = Pattern.compile("\\s+");

    private String sanitizeTime(String raw) {
        if (raw == null) return "";
        // "19: 52: 47" -> "19:52:47"
        String s = TIME_CLEANER.matcher(raw).replaceAll("");
        // 숫자와 콜론만 남도록 간단 정리하고 싶으면 아래 주석 해제
        // s = s.replaceAll("[^0-9:]", "");
        return s;
    }

    private Integer parseIntOrDefault(String s, int def) {
        if (s == null) return def;
        String digits = s.replaceAll("[^0-9-]", "");
        if (digits.isBlank()) return def;
        try { return Integer.parseInt(digits); }
        catch (Exception e) { return def; }
    }

    private Integer parseAmount(String s) {
        if (s == null) return 0;
        String digits = s.replaceAll("[^0-9-]", "");
        if (digits.isBlank()) return 0;
        try { return Integer.parseInt(digits); }
        catch (Exception e) { return 0; }
    }
}
