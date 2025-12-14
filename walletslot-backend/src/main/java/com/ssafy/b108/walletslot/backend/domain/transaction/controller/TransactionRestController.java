package com.ssafy.b108.walletslot.backend.domain.transaction.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.transaction.dto.*;
import com.ssafy.b108.walletslot.backend.domain.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Transaction")
/**
 * Transaction REST API Controller 입니다.
 * 거래내역을 다루는 성격의 메서드들을 TransactionController에서 작성하였습니다.
 * 거래내역을 슬롯에 배정해서 지출금액을 차감하는 우리 서비스 특성 상, TransactionController에 있는 메서드이더라도 대부분의 매핑 경로가 /accounts로 시작합니다.
 */
public class TransactionRestController {

    // Field
    private final TransactionService transactionService;

    // Method
    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(
            summary = "6-1-1 계좌 거래내역 전체조회",
            description = "계좌의 거래내역을 전체 조회합니다. (날짜기준 내림차순)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 계좌 거래내역 전체조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountTransactionListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountTransactionListResponseDto> getAccountTransactions(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @RequestParam(required = false) LocalDateTime cursor) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getAccountTransactions(principal.userId(), accountId, cursor));
    }

    @GetMapping("/accounts/{accountId}/transactions/history/check")
    @Operation(
            summary = "6-1-2 계좌 거래내역이 3개월 이상 있는지 조회",
            description = "계좌의 거래내역이 3개월 이상 있는지 여부를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 계좌 거래내역이 3개월 이상 있는지 조회 성공",
                            content = @Content(schema = @Schema(implementation = CheckAccountTransactionHistoryResponseDto.class))
                    )
            }
    )
    public ResponseEntity<CheckAccountTransactionHistoryResponseDto> checkAccountTransactionHistory(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.checkAccountTransactionHistory(principal.userId(), accountId));
    }

    @GetMapping("/accounts/{accountId}/slots/{accountSlotId}/transactions")
    @Operation(
            summary = "6-1-3 슬롯 거래내역 전체조회",
            description = "슬롯의 거래내역을 전체 조회합니다. (날짜기준 내림차순)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 슬롯 거래내역 전체조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountSlotTransactionListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountSlotTransactionListResponseDto> getAccountSlotTransactions(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String accountSlotId, @RequestParam(required = false) LocalDateTime cursor) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getAccountSlotTransactions(principal.userId(), accountId, accountSlotId, cursor));
    }

    @GetMapping("/accounts/{accountId}/slots/{accountSlotId}/transactions/daily-spending")
    @Operation(
            summary = "6-1-4 기준일 이후 슬롯 거래내역 조회",
            description = "슬롯 별 지출 누적그래프를 그리기 위해 기준일 이후의 거래내역을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 기준일 이후 슬롯 거래내역 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountSlotTransactionDailySpendingResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountSlotTransactionDailySpendingResponseDto> getAccountSlotTransactionDailySpending(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String accountSlotId) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getAccountSlotTransactionDailySpending(principal.userId(), accountId, accountSlotId));
    }

    @GetMapping("/accounts/{accountId}/slots/{accountSlotId}/transactions/{transactionId}")
    @Operation(
            summary = "6-1-5 슬롯 거래내역 상세조회",
            description = "슬롯의 거래내역을 상세 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 슬롯 거래내역 상세조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountSlotTransactionDetailResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountSlotTransactionDetailResponseDto> getAccountSlotTransactionDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String accountSlotId, @PathVariable String transactionId) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getAccountSlotTransactionDetail(principal.userId(), accountId, accountSlotId, transactionId));
    }

    @PatchMapping("/accounts/{accountId}/transactions/{transactionId}")
    @Operation(
            summary = "6-1-6 거래내역을 다른 슬롯으로 이동",
            description = "거래내역을 다른 슬롯으로 이동합니다. 기존 슬롯에서는 남은 예산이 다시 늘어나고, 새로운 슬롯의 예산에서 거래내역의 지출금액이 차감됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 거래내역 다른 슬롯으로 이동 성공",
                            content = @Content(schema = @Schema(implementation = ModifyTransactionResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ModifyTransactionResponseDto> modifyTransaction(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String transactionId, @RequestBody ModifyTransactionReqeustDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.modifyTransaction(principal.userId(), accountId, transactionId, request.getAccountSlotId()));
    }

    @PostMapping("/accounts/{accountId}/transactions/{transactionId}/splits")
    @Operation(
            summary = "6-2-1 거래내역 나누기",
            description = "하나의 거래내역을 여러개의 거래내역으로 나누고, 각각 다른 슬롯으로 재분배합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "[SlotService - 001] 슬롯 거래내역 나누기 성공",
                            content = @Content(schema = @Schema(implementation = AddSplitTransactionsResponseDto.class))
                    )
            }
    )
    public ResponseEntity<AddSplitTransactionsResponseDto> addSplitTransactions(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String transactionId, @RequestBody AddSplitTransactionsRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addSplitTransactions(principal.userId(), accountId, transactionId, request.getTransactions()));
    }

    @PostMapping("/accounts/{accountId}/transactions/{transactionId}/dutchpays")
    @Operation(
            summary = "6-2-2 더치페이",
            description = "하나의 거래내역에서 1/n만 남겨놓고, 나머지 항목을 미분류 슬롯으로 이동합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "[SlotService - 001] 더치페이 성공",
                            content = @Content(schema = @Schema(implementation = AddDutchPayTransactionsResponseDto.class))
                    )
            }
    )
    public ResponseEntity<AddDutchPayTransactionsResponseDto> addDutchPayTransactions(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String transactionId, @RequestParam Integer n) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addDutchPayTransactions(principal.userId(), accountId, transactionId, n));
    }
}
