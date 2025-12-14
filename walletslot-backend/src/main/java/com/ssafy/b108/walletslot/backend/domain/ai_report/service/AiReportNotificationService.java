package com.ssafy.b108.walletslot.backend.domain.ai_report.service;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint;
import com.ssafy.b108.walletslot.backend.domain.notification.repository.NotificationRepository;
import com.ssafy.b108.walletslot.backend.domain.notification.repository.PushEndpointRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.infrastructure.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportNotificationService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UserRepository userRepo;
    private final NotificationRepository notificationRepo;
    private final PushEndpointRepository pushEndpointRepo;
    private final FcmService fcmService;

    /**
     * 레포트 생성 알림 전송 (새 트랜잭션에서 DB write 허용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void notifyReportCreated(long userId,
                                    String accountUuid,
                                    String reportUuid,
                                    LocalDate start,
                                    LocalDate end) {

        // 0) 사용자/엔드포인트 확인
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            log.warn("[AI-REPORT][PUSH] user not found: {}", userId);
            return;
        }

        List<PushEndpoint> endpoints = pushEndpointRepo.findByUserOrderByIdDesc(user);
        if (endpoints == null || endpoints.isEmpty()) {
            log.warn("[AI-REPORT][PUSH] no endpoints for user={}", userId);
            return;
        }

        // 1) 알림 콘텐츠 구성
        final String title = "소비 리포트가 생성됐어요";
        final String body  = String.format("%s ~ %s 리포트를 확인해 보세요.",
                start != null ? start.format(DTF) : "시작일",
                end   != null ? end.format(DTF)   : "종료일");

        // 2) 알림 DB 기록 (초기 상태)
        Notification saved = notificationRepo.save(
                Notification.builder()
                        .user(user)
                        .title(title)
                        .body(body)
                        .type(Notification.Type.SYSTEM)
                        // 필요 시 추가 메타(계좌/리포트 UUID)도 엔티티가 지원하면 넣으세요.
                        .build()
        );
        final long notificationId = saved.getId();

        // 3) 중복 토큰 제거 후 전송
        Set<String> sentDistinct = new HashSet<>();
        boolean deliveredOnce = false;

        for (PushEndpoint ep : endpoints) {
            String token = ep.getToken();
            if (token == null || token.isBlank()) continue;
            if (!sentDistinct.add(token)) continue; // 중복 토큰 skip

            try {
                // 필요하면 데이터 페이로드(계좌/리포트 UUID)를 지원하는 sendMessage 오버로드를 사용
                fcmService.sendMessage(token, title, body);
                deliveredOnce = true;

            } catch (Exception err) {
                log.warn("[AI-REPORT][PUSH] send failed user={}, tokenTail={}, err={}",
                        userId,
                        token.length() > 8 ? token.substring(token.length() - 8) : token,
                        err.toString());
            }
        }

        // 4) 최소 1건 성공 시 배송 상태 갱신
        if (deliveredOnce) {
            try {
                notificationRepo.findById(notificationId).ifPresent(n -> {
                    try {
                        // 엔티티에 맞게 업데이트 (예: setIsDelivered / updateIsDelivered 등)
                        n.updateIsDelivered(true);
                        notificationRepo.save(n);
                    } catch (Exception e) {
                        log.warn("[AI-REPORT][PUSH] mark delivered failed: {}", e.toString());
                    }
                });
            } catch (Exception e) {
                log.warn("[AI-REPORT][PUSH] post-update fetch failed: {}", e.toString());
            }
        } else {
            log.warn("[AI-REPORT][PUSH] no successful deliveries for user={}", userId);
        }
    }
}
