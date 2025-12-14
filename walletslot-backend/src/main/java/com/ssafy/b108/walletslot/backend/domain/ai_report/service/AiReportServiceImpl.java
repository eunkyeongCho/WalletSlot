package com.ssafy.b108.walletslot.backend.domain.ai_report.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.account.repository.AccountRepository;
import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.*;
import com.ssafy.b108.walletslot.backend.domain.ai_report.repository.AiReportRepository;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.AccountSlot;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.Slot;
import com.ssafy.b108.walletslot.backend.domain.transaction.entity.Transaction;
import com.ssafy.b108.walletslot.backend.domain.transaction.repository.TransactionRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportServiceImpl implements AiReportService {

    private final AccountRepository accountRepo;
    private final AiReportRepository aiReportRepo;
    private final TransactionRepository txRepo;
    private final ObjectMapper objectMapper;

    private final AiReportPersistService aiReportPersistService; // REQUIRES_NEW 저장 전용
    private final AiReportNotificationService aiReportNotificationService; // 푸시 전담

    @Qualifier("ssafyGmsWebClient")
    private final WebClient ssafyGmsWebClient;

    @Value("${api.ssafy.gms.key:}")
    private String gmsApiKey;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 하위호환: 배치 등 기존 호출은 notify=false로 위임
    @Override
    @Transactional(readOnly = true)
    public GetAiReportResponseDto getReportByPeriod(final long userId,
                                                    final String accountId,  // UUID
                                                    final LocalDate startDate,
                                                    final LocalDate endDate,
                                                    final boolean persist) {
        return getReportByPeriod(userId, accountId, startDate, endDate, persist, false);
    }

    // 신규: notify 플래그 포함
    @Override
    @Transactional(readOnly = true)
    public GetAiReportResponseDto getReportByPeriod(final long userId,
                                                    final String accountId,  // UUID
                                                    final LocalDate startDate,
                                                    final LocalDate endDate,
                                                    final boolean persist,
                                                    final boolean notify) {

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "[AiReport - 001] invalid date range");
        }

        log.info("[AiReport - 001] START userId={}, accountId(UUID)={}, start={}, end={}, persist={}, notify={}",
                userId, accountId, startDate, endDate, persist, notify);

        // 계좌 조회 (UUID)
        final Account account = accountRepo.findByUserIdAndUuid(userId, accountId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AiReport - 003] 계좌 없음"));

        // 기간 문자열(포함 범위) 구성
        final LocalDateTime start = startDate.atStartOfDay();
        final LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
        final String startStr = TS.format(start);
        final String endStr = TS.format(end);
        log.info("[AiReport - 005] Period resolved: {} ~ {}", startStr, endStr);

        // 현재 슬롯 편성
        final List<AccountSlot> slots = new ArrayList<>(account.getAccountSlots());
        if (slots.isEmpty()) throw new AppException(ErrorCode.NOT_FOUND, "[AiReport - 006] 계좌 슬롯이 없습니다.");

        // 내부ID(Long) ↔ UUID 매핑
        final Map<Long, String> asIdToUuid = new HashMap<>();
        final Map<Long, String> slotIdToUuid = new HashMap<>();
        for (AccountSlot as : slots) {
            asIdToUuid.put(as.getId(), as.getUuid());
            Slot s = as.getSlot();
            if (s != null) slotIdToUuid.put(s.getId(), s.getUuid());
        }

        // 거래 조회/집계
        final List<Transaction> rangeTx =
                txRepo.findByAccountIdAndTransactionAtBetween(account.getId(), start, end);
        log.debug("[AiReport - 008] fetched transactions count={}", rangeTx.size());

        final Map<Long, Long> spentByAsId = new HashMap<>();
        final Map<String, Long> merchantSum = new HashMap<>();
        final Map<Long, Map<Integer, Long>> dowByAsId = new HashMap<>();

        for (Transaction t : rangeTx) {
            final Long asId = t.getAccountSlot().getId();
            spentByAsId.merge(asId, nz(t.getAmount()), Long::sum);

            if (t.getSummary() != null && !t.getSummary().isBlank()) {
                merchantSum.merge(t.getSummary(), nz(t.getAmount()), Long::sum);
            }
            try {
                LocalDateTime when = t.getTransactionAt();
                if (when != null) {
                    int dow = when.getDayOfWeek().getValue(); // 1~7
                    dowByAsId.computeIfAbsent(asId, k -> new HashMap<>())
                            .merge(dow, nz(t.getAmount()), Long::sum);
                }
            } catch (Exception ignore) {}
        }

        long totalBudget = 0L, totalSpent = 0L, totalOvers = 0L, totalUnders = 0L;
        long oversExUncls = 0L, savedExUncls = 0L;
        final List<GetAiReportResponseDto.SlotRow> slotItems = new ArrayList<>();

        for (AccountSlot as : slots) {
            final Slot slot = as.getSlot();
            final long budget = nz(as.getCurrentBudget());
            final long spent = nz(spentByAsId.getOrDefault(as.getId(), 0L));
            final long diff = budget - spent;
            final boolean exceeded = spent > budget;
            final long overs = exceeded ? (spent - budget) : 0L;
            final long unders = exceeded ? 0L : (budget - spent);

            // 스키마상 slot_id NOT NULL → 이름 기준으로만 '미분류' 판정
            final boolean isUncls = (slot != null)
                    && "미분류".equals(Optional.ofNullable(slot.getName()).orElse(""));

            totalBudget += budget;
            totalSpent += spent;
            totalOvers += overs;
            totalUnders += unders;

            if (!isUncls) {
                oversExUncls += overs;
                savedExUncls += unders;
            }

            slotItems.add(GetAiReportResponseDto.SlotRow.builder()
                    .accountSlotId(as.getUuid())
                    .slotId(slot != null ? slot.getUuid() : null)
                    .slotName(resolveName(as))
                    .unclassified(isUncls)
                    .budget(budget)
                    .spent(spent)
                    .diff(diff)
                    .exceeded(exceeded)
                    .overspend(overs)
                    .underspend(unders)
                    .baseNext(budget)
                    .allocated(0L)
                    .recommendedNextBudget(budget)
                    .deltaFromCurrent(0L)
                    .build());
        }

        // 절약액→초과 슬롯 비례 분배(천원단위)
        final long savedPool = floorK(savedExUncls);
        final long oversPool = floorK(oversExUncls);
        final Map<Long, Long> firstAlloc = new HashMap<>();
        final Map<Long, Double> frac = new HashMap<>();
        long allocatedSum = 0L;

        if (savedPool > 0 && oversPool > 0) {
            for (GetAiReportResponseDto.SlotRow r : slotItems) {
                Long asId = asIdToUuid.entrySet().stream()
                        .filter(e -> Objects.equals(e.getValue(), r.getAccountSlotId()))
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);
                if (asId == null) continue;

                if (!r.isUnclassified() && r.isExceeded() && r.getOverspend() > 0) {
                    double share = ((double) r.getOverspend() / (double) oversPool) * (double) savedPool;
                    long alloc = floorK(Math.round(share));
                    firstAlloc.put(asId, alloc);
                    allocatedSum += alloc;
                    frac.put(asId, Math.max(0d, share - alloc));
                }
            }
        }
        long remain = savedPool - allocatedSum;
        if (remain > 0 && !frac.isEmpty()) {
            final List<Long> order = frac.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            int i = 0;
            while (remain >= 1_000) {
                Long asId = order.get(i % order.size());
                firstAlloc.merge(asId, 1_000L, Long::sum);
                remain -= 1_000L;
                i++;
            }
        }

        // 추천 반영
        for (int i = 0; i < slotItems.size(); i++) {
            GetAiReportResponseDto.SlotRow r = slotItems.get(i);
            Long asId = asIdToUuid.entrySet().stream()
                    .filter(e -> Objects.equals(e.getValue(), r.getAccountSlotId()))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);
            long alloc = (asId != null) ? firstAlloc.getOrDefault(asId, 0L) : 0L;
            long next = r.getBaseNext() + alloc;
            slotItems.set(i, r.toBuilder()
                    .allocated(alloc)
                    .recommendedNextBudget(next)
                    .deltaFromCurrent(next - r.getBudget())
                    .build());
        }

        final GetAiReportResponseDto.Summary summary = GetAiReportResponseDto.Summary.builder()
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .totalOverspent(totalOvers)
                .totalUnderspent(totalUnders)
                .savedExcludingUnclassified(savedExUncls)
                .oversExcludingUnclassified(oversExUncls)
                .top3Slots(top3BySpent(slotItems))
                .build();

        final List<GetAiReportResponseDto.Share> sharesDto = firstAlloc.entrySet().stream()
                .map(e -> {
                    Long asId = e.getKey();
                    String asUuid = asIdToUuid.get(asId);
                    GetAiReportResponseDto.SlotRow s = slotItems.stream()
                            .filter(it -> Objects.equals(it.getAccountSlotId(), asUuid))
                            .findFirst().orElse(null);
                    String name = (s != null) ? s.getSlotName() : ("#" + asUuid);
                    double ratio = (oversPool == 0L) ? 0d
                            : (double) (s != null ? s.getOverspend() : 0L) / (double) oversPool;
                    return GetAiReportResponseDto.Share.builder()
                            .accountSlotId(asUuid)
                            .slotName(name)
                            .ratio(ratio)
                            .allocated(e.getValue())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getAllocated(), a.getAllocated()))
                .collect(Collectors.toList());

        final GetAiReportResponseDto.Redistribution redist = GetAiReportResponseDto.Redistribution.builder()
                .savedTotal(savedPool)
                .oversTotal(oversPool)
                .shares(sharesDto)
                .remainder(Math.max(0L, savedPool - sharesDto.stream().mapToLong(GetAiReportResponseDto.Share::getAllocated).sum()))
                .build();

        // 내부 인사이트 + GMS 콜
        // 내부 인사이트 + GMS 콜
        GetAiReportResponseDto.Insights insights = buildInsights(merchantSum, dowByAsId, asIdToUuid);
        try {
            String label = startDate + "~" + endDate;
            Map<String, Object> gms = callGmsForInsightsByTextPeriod(label, slotItems, summary);
            if (isValidGms(gms)) { // ★ 유효성 체크
                insights = insights.toBuilder()
                        .aiSummary((String) gms.get("summary"))
                        .aiActionItems((List<String>) gms.getOrDefault("actions", Collections.emptyList()))
                        .aiRaw(gms)
                        .build();
            } else {
                Heuristic fb = buildHeuristicAiText(summary, slotItems);
                insights = insights.toBuilder()
                        .aiSummary(fb.summary)
                        .aiActionItems(fb.actions)
                        .aiRaw(Map.of("summary", fb.summary, "actions", fb.actions, "source", "fallback"))
                        .build();
            }
        } catch (Exception e) {
            log.warn("[AiReport - 013] GMS insights failed: {}", e.getMessage());
        }


        final GetAiReportResponseDto.Period period = GetAiReportResponseDto.Period.builder()
                .yearMonth(null)
                .startAt(startStr)
                .endAt(endStr)
                .build();

        GetAiReportResponseDto.PersistInfo persistRef = null;
        if (persist) {
            Map<String, Object> content = new HashMap<>();
            content.put("period", period);
            content.put("summary", summary);
            content.put("slots", slotItems);
            content.put("redistribution", redist);
            content.put("insights", insights);
            persistRef = aiReportPersistService.saveInNewTx(account.getId(), content);
        }

        // 저장 성공 + notify=true 이면 알림 전송
        if (persist && notify && persistRef != null && persistRef.getId() != null) {
            try {
                aiReportNotificationService.notifyReportCreated(
                        userId, accountId, persistRef.getId(), startDate, endDate
                );
            } catch (Exception e) {
                log.warn("[AI-REPORT][PUSH] notify error: {}", e.toString());
            }
        }

        return GetAiReportResponseDto.builder()
                .success(true)
                .message("[AiReport - 015] OK")
                .data(GetAiReportResponseDto.Data.builder()
                        .period(period)
                        .summary(summary)
                        .slots(slotItems)
                        .redistribution(redist)
                        .insights(insights)
                        .persist(persistRef)
                        .build())
                .build();
    }

    // ============================= 삭제 (7-2)
    @Override
    @Transactional
    public DeleteAiReportResponseDto delete(final long userId,
                                            final String accountId,
                                            final String reportId) {
        log.info("[AiReport - 017] DELETE START userId={}, accountId(UUID)={}, reportId(UUID)={}",
                userId, accountId, reportId);

        accountRepo.findByUserIdAndUuid(userId, accountId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AiReport - 018] 계좌 없음"));

        aiReportRepo.findByUuid(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AiReport - 020] 레포트 없음"));

        aiReportRepo.deleteById(
                aiReportRepo.findByUuid(reportId).orElseThrow().getId()
        );
        log.info("[AiReport - 021] deleted reportUuid={}", reportId);

        return DeleteAiReportResponseDto.builder()
                .success(true)
                .message("[AiReport - 022] 삭제되었습니다.")
                .data(DeleteAiReportResponseDto.Data.builder().reportId(reportId).build())
                .build();
    }

    // ========================== 월 목록 (7-3-1)
    @Override
    @Transactional(readOnly = true)
    public ListAiReportMonthsResponseDto listMonths(long userId, String accountId) {
        accountRepo.findByUserIdAndUuid(userId, accountId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AiReport - 030] 계좌 없음"));

        var months = aiReportRepo.findAvailableYearMonths(userId, accountId);
        return ListAiReportMonthsResponseDto.builder()
                .success(true)
                .message("[AiReport - 031] months loaded")
                .data(ListAiReportMonthsResponseDto.Data.builder().yearMonths(months).build())
                .build();
    }

    // ====================== 월별 아카이브 (7-3-2)
    @Override
    @Transactional(readOnly = true)
    public GetAiReportArchiveResponseDto getArchiveByMonthOrOffset(long userId,
                                                                   String accountId,
                                                                   String yearMonth,
                                                                   Integer offset) {
        accountRepo.findByUserIdAndUuid(userId, accountId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AiReport - 032] 계좌 없음"));

        var months = aiReportRepo.findAvailableYearMonths(userId, accountId);
        if (months.isEmpty()) {
            return GetAiReportArchiveResponseDto.builder()
                    .success(true)
                    .message("[AiReport - 033] no reports")
                    .data(GetAiReportArchiveResponseDto.Data.builder()
                            .yearMonth(null).prevYearMonth(null).nextYearMonth(null)
                            .yearMonths(months).reports(List.of()).build())
                    .build();
        }

        String chosen;
        if (yearMonth != null && !yearMonth.isBlank() && months.contains(yearMonth)) {
            chosen = yearMonth;
        } else {
            int idx = Math.max(0, Math.min(months.size() - 1, (offset == null ? 0 : offset)));
            chosen = months.get(idx);
        }

        int index = months.indexOf(chosen);
        String prev = (index + 1 < months.size()) ? months.get(index + 1) : null;
        String next = (index - 1 >= 0) ? months.get(index - 1) : null;

        var parts = chosen.split("-");
        int y = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        var start = LocalDate.of(y, m, 1).atStartOfDay();
        var end   = start.plusMonths(1);

        var rows = aiReportRepo.findByMonth(userId, accountId, start, end);

        var items = rows.stream().map(r -> {
            var body = r.getContent();
            if (body != null && body.has("data") && body.get("data").isObject()) {
                body = body.get("data");
            }

            GetAiReportResponseDto.Period period = null;
            GetAiReportResponseDto.Summary summary = null;
            List<GetAiReportResponseDto.SlotRow> slots = List.of();
            GetAiReportResponseDto.Redistribution redist = null;
            GetAiReportResponseDto.Insights insights = null;

            try {
                if (body != null && !body.isNull()) {
                    if (body.has("period") && !body.get("period").isNull()) {
                        period = objectMapper.convertValue(body.get("period"), GetAiReportResponseDto.Period.class);
                    }
                    if (body.has("summary") && !body.get("summary").isNull()) {
                        summary = objectMapper.convertValue(body.get("summary"), GetAiReportResponseDto.Summary.class);
                    }
                    if (body.has("slots") && body.get("slots").isArray()) {
                        slots = objectMapper.convertValue(
                                body.get("slots"),
                                new TypeReference<List<GetAiReportResponseDto.SlotRow>>() {}
                        );
                    }
                    if (body.has("redistribution") && !body.get("redistribution").isNull()) {
                        redist = objectMapper.convertValue(body.get("redistribution"), GetAiReportResponseDto.Redistribution.class);
                    }
                    if (body.has("insights") && !body.get("insights").isNull()) {
                        insights = objectMapper.convertValue(body.get("insights"), GetAiReportResponseDto.Insights.class);
                    }
                }
            } catch (Exception e) {
                log.warn("[AiReport - 034A] archive mapping error reportUuid={}, msg={}", r.getUuid(), e.getMessage());
            }

            var persist = GetAiReportResponseDto.PersistInfo.builder()
                    .id(r.getUuid())
                    .createdAt(r.getCreatedAt())
                    .build();

            return GetAiReportArchiveResponseDto.Item.builder()
                    .reportId(r.getUuid())
                    .createdAt(r.getCreatedAt())
                    .period(period)
                    .summary(summary)
                    .slots(slots)
                    .redistribution(redist)
                    .insights(insights)
                    .persist(persist)
                    .build();
        }).toList();

        return GetAiReportArchiveResponseDto.builder()
                .success(true)
                .message("[AiReport - 034] archive loaded")
                .data(GetAiReportArchiveResponseDto.Data.builder()
                        .yearMonth(chosen)
                        .prevYearMonth(prev)
                        .nextYearMonth(next)
                        .yearMonths(months)
                        .reports(items)
                        .build())
                .build();
    }

    // ========================== 유틸 ==========================
    private static String resolveName(AccountSlot as) {
        if (as.isCustom() && as.getCustomName() != null) return as.getCustomName();
        Slot s = as.getSlot();
        return (s != null && s.getName() != null) ? s.getName() : "미정";
    }

    private static long floorK(long v) { return (v <= 0) ? 0L : (v / 1000) * 1000; }
    private static Long nz(Long v) { return (v == null) ? 0L : v; }

    private static List<GetAiReportResponseDto.TopSlot> top3BySpent(List<GetAiReportResponseDto.SlotRow> items) {
        return items.stream()
                .sorted((a, b) -> Long.compare(b.getSpent(), a.getSpent()))
                .limit(3)
                .map(s -> GetAiReportResponseDto.TopSlot.builder()
                        .accountSlotId(s.getAccountSlotId())
                        .slotName(s.getSlotName())
                        .spent(s.getSpent())
                        .budget(s.getBudget())
                        .build())
                .collect(Collectors.toList());
    }

    private GetAiReportResponseDto.Insights buildInsights(Map<String, Long> merchantSum,
                                                          Map<Long, Map<Integer, Long>> dowByAsId,
                                                          Map<Long, String> asIdToUuid) {
        var topMerchants = merchantSum.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> GetAiReportResponseDto.Merchant.builder()
                        .name(e.getKey())
                        .amount(e.getValue())
                        .count(0)
                        .build())
                .toList();

        Map<String, GetAiReportResponseDto.PeakDay> peak = new HashMap<>();
        for (var e : dowByAsId.entrySet()) {
            int best = 0; long max = 0;
            for (var d : e.getValue().entrySet()) {
                if (d.getValue() > max) { max = d.getValue(); best = d.getKey(); }
            }
            String asUuid = asIdToUuid.get(e.getKey());
            if (asUuid != null) {
                peak.put(asUuid, GetAiReportResponseDto.PeakDay.builder()
                        .dayOfWeek(best)
                        .amount(max)
                        .build());
            }
        }

        return GetAiReportResponseDto.Insights.builder()
                .topMerchants(topMerchants)
                .peakDayBySlot(peak)
                .notes(List.of("미분류 제외 절약액을 초과 슬롯에 비율 배분"))
                .aiSummary(null)
                .aiActionItems(Collections.emptyList())
                .aiRaw(null)
                .build();
    }

    // -------- GMS 호출: temperature 제거, 1차 JSON 강제 → 실패 시 무응답형 재시도 --------
    @SuppressWarnings("unchecked")
    private Map<String, Object> callGmsForInsightsByTextPeriod(String periodLabel,
                                                               List<GetAiReportResponseDto.SlotRow> slots,
                                                               GetAiReportResponseDto.Summary summary) {
        // 공통 메시지 구성
        var msgSystem = Map.of(
                "role", "system",
                "content", String.join("\n",
                        "너는 예산 분석가다.",
                        "한국어로 간단 요약과 3~5개의 실행항목을 JSON으로만 응답해. 이때 말투는 동글동글 하게 해라.",
                        "스키마: {\"summary\": string, \"actions\": string[] }")
        );

        String slotLines = slots.stream()
                .map(s -> String.format("%s(budget=%d, spent=%d, diff=%d%s)",
                        s.getSlotName(), s.getBudget(), s.getSpent(), s.getDiff(),
                        s.isExceeded() ? ", EXCEEDED" : ""))
                .collect(Collectors.joining("; "));

        String userContent = "period=" + periodLabel + "\n"
                + "totalBudget=" + summary.getTotalBudget()
                + ", totalSpent=" + summary.getTotalSpent()
                + ", totalUnderspent=" + summary.getTotalUnderspent()
                + ", totalOverspent=" + summary.getTotalOverspent() + "\n"
                + "slots=" + slotLines;

        var msgUser = Map.of("role", "user", "content", userContent);

        // 1차: JSON 강제
        Map<String, Object> bodyJson = new HashMap<>();
        bodyJson.put("model", "gpt-5-nano");
        bodyJson.put("messages", List.of(msgSystem, msgUser));
        bodyJson.put("response_format", Map.of("type", "json_object")); // ★ JSON 강제
        // temperature 제거 (프록시가 금지)

        try {
            return callAndParseGms(bodyJson, true);
        } catch (Exception first) {
            log.warn("[AiReport - 013] GMS JSON-mode failed ({}). Retrying without response_format...", first.toString());
            // 2차: response_format 제거(일반 텍스트 응답 → 괄호 파싱)
            Map<String, Object> bodyPlain = new HashMap<>();
            bodyPlain.put("model", "gpt-5-nano");
            bodyPlain.put("messages", List.of(msgSystem, msgUser));
            try {
                return callAndParseGms(bodyPlain, false);
            } catch (Exception second) {
                log.warn("[AiReport - 013] GMS plain-mode failed: {}", second.getMessage());
                return null;
            }
        }
    }

    private Map<String, Object> callAndParseGms(Map<String, Object> body, boolean expectJson) throws Exception {
        Map<String, Object> res = ssafyGmsWebClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class).doOnNext(err ->
                                log.warn("[AiReport - 013] GMS HTTP {}: {}", resp.statusCode(), err)
                        ).then(Mono.error(new IllegalStateException("GMS error: " + resp.statusCode())))
                )
                .bodyToMono(Map.class)
                .block(); // ★ 여기! Duration 제거 → 무기한 대기

        if (res == null) throw new IllegalStateException("GMS null response");

        String content = null;
        try {
            var choices = (List<Map<String, Object>>) res.get("choices");
            if (choices != null && !choices.isEmpty()) {
                var msg = (Map<String, Object>) choices.get(0).get("message");
                content = (msg != null) ? (String) msg.get("content") : null;
            }
        } catch (Exception ignore) {}

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("GMS empty content");
        }

        Map<String, Object> parsed;
        if (expectJson) {
            try {
                parsed = objectMapper.readValue(content, Map.class);
            } catch (Exception ex) {
                String jsonOnly = extractFirstJsonObject(content);
                if (jsonOnly == null) throw new IllegalStateException("No JSON object found in content");
                parsed = objectMapper.readValue(jsonOnly, Map.class);
            }
        } else {
            String jsonOnly = extractFirstJsonObject(content);
            if (jsonOnly == null) return null;
            parsed = objectMapper.readValue(jsonOnly, Map.class);
        }

        Map<String, Object> out = new HashMap<>();
        Object s = parsed.get("summary");
        out.put("summary", (s instanceof String) ? ((String) s).strip() : null);

        List<String> actions = new ArrayList<>();
        Object a = parsed.get("actions");
        if (a instanceof List<?> list) {
            for (Object o : list) if (o != null) actions.add(String.valueOf(o).strip());
        } else if (a instanceof String str) {
            Arrays.stream(str.split("[\\r\\n•\\-]+"))
                    .map(String::trim).filter(t -> !t.isEmpty())
                    .forEach(actions::add);
        }
        if (actions.size() > 5) actions = actions.subList(0, 5);
        out.put("actions", actions);

        return out;
    }


    // JSON 오브젝트 추출 (백업 파서)
    private static String extractFirstJsonObject(String s) {
        int depth = 0, start = -1;
        boolean inStr = false, esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (esc) { esc = false; continue; }
                if (c == '\\') { esc = true; continue; }
                if (c == '"') { inStr = false; }
                continue;
            }
            if (c == '"') { inStr = true; continue; }
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && depth > 0) { if (--depth == 0 && start >= 0) return s.substring(start, i + 1); }
        }
        return null;
    }

    // 휴리스틱 대체 텍스트
    private static class Heuristic {
        final String summary; final List<String> actions;
        Heuristic(String s, List<String> a) { summary = s; actions = a; }
    }
    private Heuristic buildHeuristicAiText(GetAiReportResponseDto.Summary s,
                                           List<GetAiReportResponseDto.SlotRow> items) {
        String sum = String.format("총 지출 %,d원, 예산 대비 절약 %,d원, 초과 %,d원.",
                s.getTotalSpent(), s.getTotalUnderspent(), s.getTotalOverspent());

        List<String> actions = new ArrayList<>();
        if (s.getTotalOverspent() == 0) {
            actions.add("이번 기간엔 초과 지출이 없었습니다. 현재 예산을 유지해 보세요.");
        } else {
            items.stream().filter(GetAiReportResponseDto.SlotRow::isExceeded)
                    .sorted(Comparator.comparingLong(GetAiReportResponseDto.SlotRow::getOverspend).reversed())
                    .limit(3)
                    .forEach(r -> actions.add(r.getSlotName() + " 예산 상향 또는 지출 원인 점검"));
        }
        if (s.getSavedExcludingUnclassified() > 0) {
            actions.add("절약액 일부를 초과 위험 슬롯에 분배하는 것을 고려하세요.");
        }
        if (actions.size() < 3) actions.add("주간 단위로 결제 알림을 켜서 지출 피크일을 관리하세요.");

        return new Heuristic(sum, actions);
    }

    // 헬퍼 추가
    private boolean isValidGms(Map<String, Object> gms) {
        if (gms == null) return false;
        String s = (String) gms.get("summary");
        @SuppressWarnings("unchecked")
        List<String> a = (List<String>) gms.get("actions");
        boolean hasSummary = (s != null && !s.isBlank());
        boolean hasActions = (a != null && !a.isEmpty());
        return hasSummary || hasActions;
    }

}
