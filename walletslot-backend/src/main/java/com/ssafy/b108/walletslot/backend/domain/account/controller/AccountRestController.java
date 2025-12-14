package com.ssafy.b108.walletslot.backend.domain.account.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.account.dto.*;
import com.ssafy.b108.walletslot.backend.domain.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountRestController {

    // Field
    private final AccountService accountService;

    // Method
    @PostMapping
    @Operation(
            summary = "4-1-1 마이데이터 연동",
            description = "사용자가 선택한 은행에 있는 마이데이터를 불러옵니다. (from. SSAFY 교육용 금융망 API)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 마이데이터 연동 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountsResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountsResponseDto> getAccountList(@AuthenticationPrincipal UserPrincipal principal, @RequestBody GetAccountsRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccounts(principal.userId(), request.getBanks()));
    }

    @GetMapping("/link")
    @Operation(
            summary = "4-1-2 연동 계좌목록 조회",
            description = "사용자가 우리 서비스에 연동한 계좌목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 연동계좌 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetLinkedAccountListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetLinkedAccountListResponseDto> getLinkedAccounts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getLinkedAccounts(principal.userId()));
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "4-1-3 계좌 상세조회",
            description = "계좌를 상세조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 계좌 상세조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountsResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountResponseDto> getAccount(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccount(principal.userId(), accountId));
    }

    @GetMapping("/primary")
    @Operation(
            summary = "4-1-4 대표계좌 상세조회",
            description = "사용자의 대표 계좌를 상세조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 대표계좌 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetPrimaryAccountResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetPrimaryAccountResponseDto> getPrimaryAccount(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getPrimaryAccount(principal.userId()));
    }

    @GetMapping("/{accountId}/balance")
    @Operation(
            summary = "4-1-5 계좌 잔액조회",
            description = "계좌의 잔액을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 계좌 잔액 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountBalanceResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountBalanceResponseDto> getBalance(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccountBalance(principal.userId(), accountId));
    }

    @DeleteMapping("/{accountId}")
    @Operation(
            summary = "4-1-6 연동된 계좌 삭제",
            description = "사용자가 우리 서비스에 연동했던 계좌를 연동 해제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 계좌 삭제 성공",
                            content = @Content(schema = @Schema(implementation = DeleteLinkedAccountResponseDto.class))
                    )
            }
    )
    public ResponseEntity<DeleteLinkedAccountResponseDto> deleteAccount(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.deleteLinkedAccount(principal.userId(), accountId));
    }

    @PostMapping("/verification/request")
    @Operation(
            summary = "4-2-1 1원인증 요청",
            description = "1원인증을 요청합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "[AccountService - 000] 1원인증 요청 성공",
                            content = @Content(schema = @Schema(implementation = RequestVerificationResponseDto.class))
                    )
            }

    )
    public ResponseEntity<RequestVerificationResponseDto> requestVerification(@RequestBody RequestVerificationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.requestVerification(request.getUserName(), request.getBankId(), request.getAccountNo()));
    }

    @PostMapping("/verification/verify")
    @Operation(
            summary = "4-2-2 1원인증",
            description = "1원 인증을 진행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 1원인증 검증 결과: 인증번호 일치 or 불일치",
                            content = @Content(schema = @Schema(implementation = VerifyAccountResponseDto.class))
                    )
            }
    )
    public ResponseEntity<VerifyAccountResponseDto> verifyAccount(@RequestBody VerifyAccountRequestDto request) {
        int indexOfSpace = request.getAuthIdentifier().indexOf(" ");
        String authText = request.getAuthIdentifier().substring(0, indexOfSpace);
        String authCode = request.getAuthIdentifier().substring(indexOfSpace + 1);

        return ResponseEntity.status(HttpStatus.OK).body(accountService.verifyAccount(request.getUserName(), request.getAccountNo(), authText, authCode));
    }

    @PostMapping("/link")
    @Operation(
            summary = "4-3-1 계좌연동",
            description = "사용자의 계좌를 우리 서비스에 연동합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "[AccountService - 000] 계좌연동 성공",
                            content = @Content(schema = @Schema(implementation = AddAccountResponseDto.class))
                    )
            }
    )
    public ResponseEntity<AddAccountResponseDto> addAccount(@AuthenticationPrincipal UserPrincipal principal, @RequestBody AddAccountRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.addAccount(principal.userId(), request.getAccounts()));
    }

    @PatchMapping("/{accountId}")
    @Operation(
            summary = "4-3-2 계좌 정보 수정",
            description = "특정계좌를 대표계좌로 설정하거나 별칭을 지정 및 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[AccountService - 000] 계좌정보 수정 성공",
                            content = @Content(schema = @Schema(implementation = ModifyAccountResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ModifyAccountResponseDto> modifyAccount(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @RequestBody ModifyAccountRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.modifyAccount(principal.userId(), accountId, request));
    }
}
