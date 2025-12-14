package com.ssafy.b108.walletslot.backend.domain.notification.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.notification.dto.notification.*;
import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import com.ssafy.b108.walletslot.backend.domain.notification.service.NotificationService;
import com.ssafy.b108.walletslot.backend.global.dto.ErrorResponse;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Notification",
        description = """
            알림 생성/조회/읽음/삭제/Pull API.
            """
)
public class NotificationRestController {

    private final NotificationService service;

    @PostMapping
    @Operation(
            summary = "8-2-1 알림 생성(특정 사용자)",
            description = """
                특정 사용자에게 일반 알림을 생성합니다.
                - 관리(운영/백오피스)용 API 가정
                - 앱 클라이언트는 주로 Pull/목록/읽음/삭제 API를 사용
                - 본 엔드포인트는 트랜잭션 연계가 없으므로 transactionUuid는 응답에서 null
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "1")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateNotificationRequestDto.class),
                            examples = @ExampleObject(name = "req",
                                    value = """
                                    {
                                      "targetUserId": 1,
                                      "title": "예산 초과 알림",
                                      "content": "이번달 카페/간식 예산을 초과했습니다.",
                                      "type": "BUDGET"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateNotificationResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "[NotificationService - 001] 알림 생성 성공",
                                      "data": {
                                        "notification": {
                                          "uuid": "5a9d3c9a-1b1f-4c6f-9f8a-8ad3a1e4b9c1",
                                          "title": "예산 초과 알림",
                                          "body": "이번달 카페/간식 예산을 초과했습니다.",
                                          "type": "BUDGET",
                                          "isRead": false,
                                          "isDelivered": false,
                                          "readAt": null,
                                          "deliveredAt": null,
                                          "transactionUuid": null
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "대상 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound",
                                    value = """
                                    { "success": false, "message": "[NotificationService - 001] 리소스를 찾을 수 없습니다." }
                                    """)))
    })
    public ResponseEntity<CreateNotificationResponseDto> create(
            @RequestBody final CreateNotificationRequestDto req
    ) {
        return ResponseEntity.ok(service.create(req));
    }

    @PostMapping("/pull")
    @Operation(
            summary = "8-2-2 미전송 Pull + delivered 처리",
            description = """
                서버에 쌓여있던 '미전송' 알림을 한 번에 가져오고, 가져온 항목을 delivered 처리합니다.
                - 읽음 여부(isRead)와는 별개
                - 모바일 최초 진입/재설치 시 동기화 용도
                - 응답 DTO에는 transactionUuid가 포함될 수 있습니다(미분류/거래연계 알림의 경우).
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "2"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pull 성공",
                    content = @Content(schema = @Schema(implementation = PullNotificationListResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "[NotificationService - 002] 미전송 알림 Pull + delivered 처리 성공",
                                      "data": {
                                        "notifications": [
                                          {
                                            "uuid": "11111111-1111-1111-1111-111111111111",
                                            "title": "분류되지 않은 지출이 있어요",
                                            "body": "스타벅스 5,800원 • 2025-09-28T12:34:56\\nSTARBUCKS HONGDAE\\n카테고리를 지정해 주세요.",
                                            "type": "UNCATEGORIZED",
                                            "isRead": false,
                                            "isDelivered": true,
                                            "readAt": null,
                                            "deliveredAt": "2025-09-28T13:10:00",
                                            "transactionUuid": "tx-uuid-1234"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "unauthorized",
                                    value = """
                                    { "success": false, "message": "로그인이 필요합니다." }
                                    """)))
    })
    public ResponseEntity<PullNotificationListResponseDto> pull(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.pullUndelivered(principal.userId()));
    }

    @GetMapping
    @Operation(
            summary = "8-2-3 목록 조회(8-2-9 type 필터 포함)",
            description = """
                최신순으로 알림 목록을 페이지네이션 조회합니다.
                - type 파라미터로 특정 종류만 필터링 가능 (예: /api/notifications?type=BUDGET&page=0&size=20)
                - 응답 DTO에는 transactionUuid가 포함될 수 있습니다.
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "3"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetNotificationPageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GetNotificationPageResponseDto> list(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal,
            @Parameter(description = "알림 유형 필터", schema = @Schema(implementation = Notification.Type.class))
            @RequestParam(required = false) final String type,
            @ParameterObject final Pageable pageable
    ) {
        if (principal == null) throw new AppException(ErrorCode.UNAUTHORIZED, "[NotificationController - list]");
        final Pageable pg = clamp(pageable);
        final Notification.Type t = parseTypeOrNull(type, "[NotificationController - list]");
        return ResponseEntity.ok(service.getNotificationPage(principal.userId(), t, pg));
    }

    @GetMapping("/unread")
    @Operation(
            summary = "8-2-3-1 미읽음 목록 조회(8-2-9 type 필터 포함)",
            description = """
                읽지 않은(isRead=false) 알림만 최신순 페이지 조회합니다.
                - type 파라미터로 특정 종류만 필터링 가능
                - 응답 DTO에는 transactionUuid가 포함될 수 있습니다.
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "3-1"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetNotificationPageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GetNotificationPageResponseDto> unreadList(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal,
            @Parameter(description = "알림 유형 필터", schema = @Schema(implementation = Notification.Type.class))
            @RequestParam(required = false) final String type,
            @ParameterObject final Pageable pageable
    ) {
        if (principal == null) throw new AppException(ErrorCode.UNAUTHORIZED, "[NotificationController - unreadList]");
        final Pageable pg = clamp(pageable);
        final Notification.Type t = parseTypeOrNull(type, "[NotificationController - unreadList]");
        return ResponseEntity.ok(service.getUnreadPage(principal.userId(), t, pg));
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "8-2-4 미읽음 개수",
            description = "현재 계정의 전체 미읽음 알림 개수를 반환합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "4"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CountUnreadResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "[NotificationService - 004] 미읽음 개수 조회 성공",
                                      "data": { "count": 3 }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CountUnreadResponseDto> unreadCount(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.unreadCount(principal.userId()));
    }

    @GetMapping("/unread-count/by-type")
    @Operation(
            summary = "8-2-4-1 미읽음 개수(타입별)",
            description = "type을 지정해 미읽음 개수를 조회합니다. 미지정 시 UNCATEGORIZED.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "4-1"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CountUnreadByTypeResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "[NotificationService - 004T] 타입별 미읽음 개수 조회 성공",
                                      "data": { "count": 2, "type": "UNCATEGORIZED" }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CountUnreadByTypeResponseDto> unreadCountByType(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal,
            @Parameter(description = "알림 유형(기본: UNCATEGORIZED)",
                    schema = @Schema(implementation = Notification.Type.class))
            @RequestParam(required = false) final String type
    ) {
        if (principal == null) throw new AppException(ErrorCode.UNAUTHORIZED, "[NotificationController - unreadCountByType]");
        final Notification.Type t = (type == null || type.isBlank())
                ? Notification.Type.UNCATEGORIZED
                : parseTypeOrThrow(type, "[NotificationController - unreadCountByType]");
        return ResponseEntity.ok(service.unreadCountByType(principal.userId(), t));
    }

    @PatchMapping("/{notificationUuid}/delivered")
    @Operation(
            summary = "8-2-5 단건 delivered",
            description = "특정 알림(UUID)을 delivered 처리합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "5"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공",
                    content = @Content(schema = @Schema(implementation = SimpleOkResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    { "success": true, "message": "[NotificationService - 005] delivered 처리 성공" }
                                    """))),
            @ApiResponse(responseCode = "404", description = "대상 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound",
                                    value = """
                                    { "success": false, "message": "[NotificationService - 005] notification 리소스를 찾을 수 없습니다." }
                                    """)))
    })
    public ResponseEntity<SimpleOkResponseDto> delivered(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal,
            @Parameter(description = "대상 알림 UUID", example = "5a9d3c9a-1b1f-4c6f-9f8a-8ad3a1e4b9c1")
            @PathVariable("notificationUuid") final String notificationUuid
    ) {
        return ResponseEntity.ok(service.markDelivered(principal.userId(), notificationUuid));
    }

    @PatchMapping("/{notificationUuid}/read")
    @Operation(
            summary = "8-2-6 단건 읽음",
            description = "특정 알림(UUID)을 읽음 처리합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "6"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공",
                    content = @Content(schema = @Schema(implementation = SimpleOkResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    { "success": true, "message": "[NotificationService - 006] 읽음 처리 성공" }
                                    """))),
            @ApiResponse(responseCode = "404", description = "대상 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound",
                                    value = """
                                    { "success": false, "message": "[NotificationService - 006] notification 리소스를 찾을 수 없습니다." }
                                    """)))
    })
    public ResponseEntity<SimpleOkResponseDto> read(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal,
            @Parameter(description = "대상 알림 UUID", example = "3c1e5f6a-7b22-4f24-9f55-9e0aef928f80")
            @PathVariable("notificationUuid") final String notificationUuid
    ) {
        return ResponseEntity.ok(service.markRead(principal.userId(), notificationUuid));
    }

    @PostMapping("/read-all")
    @Operation(
            summary = "8-2-7 전체 읽음",
            description = "내 모든 알림을 읽음 처리합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "7"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공",
                    content = @Content(schema = @Schema(implementation = SimpleOkResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    { "success": true, "message": "[NotificationService - 007] 전체 읽음 처리 성공" }
                                    """)))
    })
    public ResponseEntity<SimpleOkResponseDto> readAll(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal
    ) {
        return ResponseEntity.ok(service.markAllRead(principal.userId()));
    }

    @DeleteMapping("/{notificationUuid}")
    @Operation(
            summary = "8-2-8 알림 삭제",
            description = "특정 알림(UUID)을 삭제합니다.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "8"))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = DeleteNotificationResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "[NotificationService - 008] 알림 삭제 성공",
                                      "data": {
                                        "notification": {
                                          "uuid": "5a9d3c9a-1b1f-4c6f-9f8a-8ad3a1e4b9c1",
                                          "title": "분류되지 않은 지출이 있어요",
                                          "body": "스타벅스 5,800원 • 2025-09-28T12:34:56\\nSTARBUCKS HONGDAE\\n카테고리를 지정해 주세요.",
                                          "type": "UNCATEGORIZED",
                                          "isRead": false,
                                          "isDelivered": true,
                                          "readAt": null,
                                          "deliveredAt": "2025-09-28T13:10:00",
                                          "transactionUuid": "tx-uuid-1234"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "대상 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeleteNotificationResponseDto> delete(
            @AuthenticationPrincipal @Parameter(hidden = true) final UserPrincipal principal,
            @Parameter(description = "대상 알림 UUID", example = "5a9d3c9a-1b1f-4c6f-9f8a-8ad3a1e4b9c1")
            @PathVariable("notificationUuid") final String notificationUuid
    ) {
        return ResponseEntity.ok(service.delete(principal.userId(), notificationUuid));
    }

    // 미분류 슬롯 전용 알림 생성(내부/운영용 혹은 분류 실패 시 서버에서 호출)
    @PostMapping("/uncategorized")
    @Operation(
            summary = "U-1 미분류 슬롯 전용 알림 생성(내부/운영용)",
            description = """
                트랜잭션 분류 실패(미분류) 시 서버에서 호출하는 전용 생성 API.
                - 생성된 알림의 type=UNCATEGORIZED
                - 팀 규칙: DB에는 transaction의 id(tx_id)만 저장, 응답 DTO에는 transactionUuid로 내려갑니다.
                """,
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "U1")),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NotifyUncategorizedRequestDto.class),
                            examples = @ExampleObject(name = "req", value = """
                            {
                              "targetUserId": 1,
                              "merchant": "스타벅스",
                              "amount": 5800,
                              "transactionSummary": "STARBUCKS HONGDAE",
                              "transactionUuid": "tx-uuid-1234",
                              "transactionAt": "2025-09-28T12:34:56"
                            }
                            """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateNotificationResponseDto.class),
                            examples = @ExampleObject(name = "ok",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "[NotificationService - U01] 미분류 슬롯 알림 생성 성공",
                                      "data": {
                                        "notification": {
                                          "uuid": "22222222-2222-2222-2222-222222222222",
                                          "title": "분류되지 않은 지출이 있어요",
                                          "body": "스타벅스 5,800원 • 2025-09-28T12:34:56\\nSTARBUCKS HONGDAE\\n카테고리를 지정해 주세요.",
                                          "type": "UNCATEGORIZED",
                                          "isRead": false,
                                          "isDelivered": false,
                                          "readAt": null,
                                          "deliveredAt": null,
                                          "transactionUuid": "tx-uuid-1234"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "대상 사용자/트랜잭션 없음(정책에 맞게 처리)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CreateNotificationResponseDto> notifyUncategorized(
            @RequestBody final NotifyUncategorizedRequestDto req
    ) {
        return ResponseEntity.ok(service.notifyUncategorized(req));
    }

    /* ----------------- private helpers ----------------- */

    private Pageable clamp(Pageable pageable) {
        if (pageable == null) return PageRequest.of(0, 20);
        int page = Math.max(0, pageable.getPageNumber());
        int size = pageable.getPageSize() <= 0 ? 20 : Math.min(pageable.getPageSize(), 50);
        return PageRequest.of(page, size);
    }

    private Notification.Type parseTypeOrNull(String type, String loc) {
        if (type == null || type.isBlank()) return null;
        try {
            return Notification.Type.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, loc + " invalid type: " + type);
        }
    }

    private Notification.Type parseTypeOrThrow(String type, String loc) {
        try {
            return Notification.Type.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, loc + " invalid type: " + type);
        }
    }
}
