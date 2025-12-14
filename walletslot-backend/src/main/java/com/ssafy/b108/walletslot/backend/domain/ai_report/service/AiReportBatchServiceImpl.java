package com.ssafy.b108.walletslot.backend.domain.ai_report.service;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.account.repository.AccountRepository;
import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.GetAiReportResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ai_report.repository.AiReportRepository;
import com.ssafy.b108.walletslot.backend.domain.ai_report.support.BillingPeriod;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint;
import com.ssafy.b108.walletslot.backend.domain.notification.repository.NotificationRepository;
import com.ssafy.b108.walletslot.backend.domain.notification.repository.PushEndpointRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import com.ssafy.b108.walletslot.backend.infrastructure.fcm.service.FcmService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportBatchServiceImpl implements AiReportBatchService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final AiReportRepository aiReportRepo;
    private final AiReportService aiReportService;           // already exists in your codebase
    private final NotificationRepository notificationRepo;
    private final PushEndpointRepository pushEndpointRepo;
    private final FcmService fcmService;                     // your existing FCM sender

    @Override
    @Transactional
    public void generateForAllUsersIfDue(LocalDate today) {
        int dom = today.getDayOfMonth();
        int last = today.lengthOfMonth();

        // 1) Users whose baseDay == today
        List<User> targets = new ArrayList<>();
        targets.addAll(userRepo.findAllByBaseDay((short) dom));

        // 2) Month-end fallback (e.g., baseDay=31 but month has 30 days ⇒ run on 30th)
        if (dom == last) {
            targets.addAll(userRepo.findAllByBaseDayGreaterThan((short) last));
        }

        if (targets.isEmpty()) {
            log.info("[AI-REPORT][BATCH] No users due for {}", today);
            return;
        }

        for (User u : targets) {
            try {
                Short baseDay = u.getBaseDay();
                if (baseDay == null) continue; // safety

                BillingPeriod period = BillingPeriod.forBaseDay(today, baseDay);

                // Iterate all accounts for this user
                List<Account> accounts = accountRepo.findByUser(u);
                for (Account acc : accounts) {
                    // prevent duplicate generation if something already created today for this account
                    LocalDateTime dayStart = today.atStartOfDay();
                    LocalDateTime nextDay = dayStart.plusDays(1);
                    boolean existsToday = aiReportRepo.existsByAccount_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(acc.getId(), dayStart, nextDay);
                    if (existsToday) {
                        log.info("[AI-REPORT][SKIP] Already generated today: user={}, account={}", u.getId(), acc.getUuid());
                        continue;
                    }

                    // Build & persist report using your existing service (persist=true)
                    GetAiReportResponseDto dto = aiReportService.getReportByPeriod(
                            u.getId(), acc.getUuid(), period.start(), period.end(), true
                    );

                    // Notify user (best-effort; do not fail batch if device missing)
                    try {
                        sendReportCreatedNotification(u, period);
                    } catch (AppException ex) {
                        if (ex.getErrorCode() == ErrorCode.MISSING_PUSH_ENDPOINT) {
                            log.warn("[AI-REPORT][NOTIFY] No device for user={}, skipping FCM", u.getId());
                        } else {
                            log.warn("[AI-REPORT][NOTIFY] {}: {}", ex.getErrorCode(), ex.getMessage());
                        }
                    } catch (Exception e) {
                        log.warn("[AI-REPORT][NOTIFY] Unexpected error: {}", e.toString());
                    }
                }
            } catch (Exception e) {
                log.error("[AI-REPORT][BATCH] userId={} failed: {}", u.getId(), e.toString());
            }
        }
    }

    private void sendReportCreatedNotification(User user, BillingPeriod period) {
        String title = "월간 리포트가 도착했어요";
        String body  = String.format("%s ~ %s 리포트를 확인해 보세요.", period.start(), period.end());

        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(Notification.Type.SYSTEM)
                .build();
        notificationRepo.save(n);

        List<PushEndpoint> endpoints = pushEndpointRepo.findByUserOrderByIdDesc(user);
        if (endpoints == null || endpoints.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_PUSH_ENDPOINT, "[AI-REPORT][NOTIFY] No endpoints");
        }

        // Try all endpoints; mark delivered if any succeed
        final long notificationId = n.getId();
        for (PushEndpoint ep : endpoints) {
            String token = ep.getToken();
            if (token == null || token.isBlank()) continue;

            fcmService.sendMessage(token, title, body)
                    .subscribe(
                            ok -> {
                                Notification saved = notificationRepo.findById(notificationId)
                                        .orElse(null);
                                if (saved != null) {
                                    saved.updateIsDelivered(true);
                                    notificationRepo.save(saved);
                                }
                            },
                            err -> {
                                // Leave as undelivered; will be retried by your pull API if needed
                                log.warn("[AI-REPORT][FCM] send failed for user={}: {}", user.getId(), err.toString());
                            }
                    );
        }
    }
}