package com.ssafy.b108.walletslot.backend.domain.notification.service;

import com.ssafy.b108.walletslot.backend.domain.notification.dto.notification.*;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import com.ssafy.b108.walletslot.backend.domain.notification.repository.NotificationRepository;
import com.ssafy.b108.walletslot.backend.domain.transaction.repository.TransactionRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repo;
    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    // 내부 헬퍼: Notification -> DTO (tx_id → transactionUuid 역변환)
    private NotificationDto toDto(Notification n) {
        String txUuid = null;
        if (n.getTxId() != null) {
            txUuid = txRepo.findUuidById(n.getTxId()).orElse(null);
        }
        return NotificationDto.from(n, txUuid);
    }

    @Override
    public CreateNotificationResponseDto create(final CreateNotificationRequestDto req) {
        final User user = userRepo.findById(req.getTargetUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 001]"));

        Notification n = Notification.builder()
                .user(user)
                .title(req.getTitle())
                .body(req.getContent())
                .type(req.getType())
                .isRead(false)
                .isDelivered(false)
                .build();

        n = repo.save(n);

        return CreateNotificationResponseDto.builder()
                .success(true)
                .message("[NotificationService - 001] 알림 생성 성공")
                .data(CreateNotificationResponseDto.Data.builder()
                        .notification(toDto(n))
                        .build())
                .build();
    }

    @Override
    public PullNotificationListResponseDto pullUndelivered(final long userId) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 002]"));

        final var list = repo.findUndeliveredByUser(user);
        list.forEach(Notification::markDelivered);

        return PullNotificationListResponseDto.builder()
                .success(true)
                .message("[NotificationService - 002] 미전송 알림 Pull + delivered 처리 성공")
                .data(PullNotificationListResponseDto.Data.builder()
                        .notifications(list.stream().map(this::toDto).toList())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetNotificationPageResponseDto getNotificationPage(final long userId, final Notification.Type type, final Pageable pageable) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 003]"));

        final Page<Notification> page = (type == null)
                ? repo.findByUserOrderByIdDesc(user, pageable)
                : repo.findByUserAndTypeOrderByIdDesc(user, type, pageable);

        return GetNotificationPageResponseDto.builder()
                .success(true)
                .message("[NotificationService - 003] 알림 목록 조회 성공")
                .data(GetNotificationPageResponseDto.Data.builder()
                        .content(page.getContent().stream().map(this::toDto).toList())
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetNotificationPageResponseDto getUnreadPage(final long userId, final Notification.Type type, final Pageable pageable) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 003U]"));

        final Page<Notification> page = (type == null)
                ? repo.findByUserAndIsReadFalseOrderByIdDesc(user, pageable)
                : repo.findByUserAndTypeAndIsReadFalseOrderByIdDesc(user, type, pageable);

        return GetNotificationPageResponseDto.builder()
                .success(true)
                .message("[NotificationService - 003U] 미읽음 목록 조회 성공")
                .data(GetNotificationPageResponseDto.Data.builder()
                        .content(page.getContent().stream().map(this::toDto).toList())
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CountUnreadResponseDto unreadCount(final long userId) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 004]"));

        final long cnt = repo.countByUserAndIsReadFalse(user);

        return CountUnreadResponseDto.builder()
                .success(true)
                .message("[NotificationService - 004] 미읽음 개수 조회 성공")
                .data(CountUnreadResponseDto.Data.builder().count(cnt).build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CountUnreadByTypeResponseDto unreadCountByType(final long userId, Notification.Type type) {
        if (type == null) type = Notification.Type.UNCATEGORIZED;

        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 004T]"));

        final long cnt = repo.countByUserAndIsReadFalseAndType(user, type);

        return CountUnreadByTypeResponseDto.builder()
                .success(true)
                .message("[NotificationService - 004T] 타입별 미읽음 개수 조회 성공")
                .data(CountUnreadByTypeResponseDto.Data.builder()
                        .count(cnt)
                        .type(type)
                        .build())
                .build();
    }

    @Override
    public SimpleOkResponseDto markDelivered(final long userId, final String notificationUuid) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 005] user"));

        final Notification n = repo.findByUuidAndUser(notificationUuid, user)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 005] notification"));

        n.markDelivered();

        return SimpleOkResponseDto.builder()
                .success(true)
                .message("[NotificationService - 005] delivered 처리 성공")
                .build();
    }

    @Override
    public SimpleOkResponseDto markRead(final long userId, final String notificationUuid) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 006]"));

        final Notification n = repo.findByUuidAndUser(notificationUuid, user)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 006]"));

        n.markRead();

        return SimpleOkResponseDto.builder()
                .success(true)
                .message("[NotificationService - 006] 읽음 처리 성공")
                .build();
    }

    @Override
    public SimpleOkResponseDto markAllRead(final long userId) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 007]"));

        repo.findByUserOrderByIdDesc(user, Pageable.unpaged())
                .forEach(Notification::markRead);

        return SimpleOkResponseDto.builder()
                .success(true)
                .message("[NotificationService - 007] 전체 읽음 처리 성공")
                .build();
    }

    @Override
    public DeleteNotificationResponseDto delete(final long userId, final String notificationUuid) {
        final User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 008]"));

        final Notification n = repo.findByUuidAndUser(notificationUuid, user)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - 008]"));

        final NotificationDto snapshot = toDto(n);
        repo.delete(n);

        return DeleteNotificationResponseDto.builder()
                .success(true)
                .message("[NotificationService - 008] 알림 삭제 성공")
                .data(DeleteNotificationResponseDto.Data.builder().notification(snapshot).build())
                .build();
    }

    /** 미분류 전용 알림 생성: 저장은 tx_id, 응답은 transactionUuid */
    @Override
    public CreateNotificationResponseDto notifyUncategorized(final NotifyUncategorizedRequestDto req) {
        final User user = userRepo.findById(req.getTargetUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[NotificationService - U01]"));

        // 1) 요청에 transactionUuid가 오면 tx_id로 변환
        Long txId = null;
        if (req.getTransactionUuid() != null && !req.getTransactionUuid().isBlank()) {
            txId = txRepo.findIdByUuid(req.getTransactionUuid()).orElse(null);

            // 1-1) tx_id가 있고, 같은 유저/타입/tx_id로 이미 알림 있으면 그대로 반환
            if (txId != null) {
                var existed = repo.findFirstByUserAndTypeAndTxId(user, Notification.Type.UNCATEGORIZED, txId);
                if (existed.isPresent()) {
                    return CreateNotificationResponseDto.builder()
                            .success(true)
                            .message("[NotificationService - U01] 이미 존재하는 미분류 알림 반환")
                            .data(CreateNotificationResponseDto.Data.builder()
                                    .notification(toDto(existed.get()))
                                    .build())
                            .build();
                }
            }
        }

        // 2) 제목/본문 템플릿
        final String title = "분류되지 않은 지출이 있어요";
        final StringBuilder body = new StringBuilder();
        if (req.getMerchant() != null && !req.getMerchant().isBlank()) body.append(req.getMerchant()).append(" ");
        if (req.getAmount() != null) body.append(String.format("%,d원", req.getAmount()));
        if (req.getTransactionAt() != null) body.append(" • ").append(req.getTransactionAt());
        if (req.getTransactionSummary() != null && !req.getTransactionSummary().isBlank()) {
            body.append("\n").append(req.getTransactionSummary());
        }
        body.append("\n카테고리를 지정해 주세요.");

        // 3) 저장: DB에는 tx_id만
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .body(body.toString())
                .type(Notification.Type.UNCATEGORIZED)
                .isRead(false)
                .isDelivered(false)
                .txId(txId)
                .build();

        n = repo.save(n);

        // 4) 응답: DTO에는 transactionUuid (id→uuid 역변환)
        return CreateNotificationResponseDto.builder()
                .success(true)
                .message("[NotificationService - U01] 미분류 슬롯 알림 생성 성공")
                .data(CreateNotificationResponseDto.Data.builder()
                        .notification(toDto(n))
                        .build())
                .build();
    }
}
