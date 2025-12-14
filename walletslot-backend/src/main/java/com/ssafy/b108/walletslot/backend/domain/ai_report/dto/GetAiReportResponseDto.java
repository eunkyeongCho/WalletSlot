package com.ssafy.b108.walletslot.backend.domain.ai_report.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAiReportResponseDto {

    private boolean success;
    private String message;
    private Data data;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Data {
        private Period period;
        private Summary summary;
        private List<SlotRow> slots;
        private Redistribution redistribution;
        private Insights insights;
        private PersistInfo persist;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Period {
        private String yearMonth; // null 또는 "2025-08"
        private String startAt;   // "yyyy-MM-dd HH:mm:ss"
        private String endAt;     // "
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Summary {
        private long totalBudget;
        private long totalSpent;
        private long totalOverspent;
        private long totalUnderspent;
        private long savedExcludingUnclassified;
        private long oversExcludingUnclassified;
        private List<TopSlot> top3Slots;
    }

    @Getter
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotRow {
        // 외부 통신은 UUID, 이름은 id 유지
        private String accountSlotId;
        private String slotId;

        private String slotName;
        private boolean unclassified;

        private long budget;
        private long spent;
        private long diff;
        private boolean exceeded;
        private long overspend;
        private long underspend;

        private long baseNext;
        private long allocated;              // 천원 단위
        private long recommendedNextBudget;  // 천원 단위
        private long deltaFromCurrent;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Share {
        private String accountSlotId; // UUID
        private String slotName;
        private double ratio;
        private long allocated; // 천원 단위
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopSlot {
        private String accountSlotId; // UUID
        private String slotName;
        private long spent;
        private long budget;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Merchant {
        private String name;
        private long amount;
        private int count;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PeakDay {
        private int dayOfWeek; // 1~7
        private long amount;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Redistribution {
        private long savedTotal;
        private long oversTotal;
        private List<Share> shares;
        private long remainder;
    }

    @Getter
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insights {
        private List<Merchant> topMerchants;
        // key도 accountSlotId(UUID)
        private Map<String, PeakDay> peakDayBySlot;
        private List<String> notes;

        private String aiSummary;
        private List<String> aiActionItems;
        private Map<String, Object> aiRaw;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PersistInfo {
        // 외부 통신은 UUID만, 이름은 id 유지
        private String id;
        private LocalDateTime createdAt;
    }
}
