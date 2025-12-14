package com.ssafy.b108.walletslot.backend.domain.slot.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.slot.dto.*;
import com.ssafy.b108.walletslot.backend.domain.slot.service.SlotService;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Slot")
/**
 * Slot REST API Controller 입니다.
 * 슬롯을 다루는 성격의 메서드들을 SlotController에서 작성하였습니다.
 * Account - Slot 간 연결관계가 강한 우리 서비스 특성 상, SlotController에 있는 메서드이더라도 대부분의 매핑 경로가 /accounts로 시작합니다.
 */
public class SlotRestController {

    // Field
    private final SlotService slotService;

    // Method
    @GetMapping("/slots")
    @Operation(
            summary = "5-1-1 슬롯 전체조회",
            description = "사용자에게 제공할 슬롯의 종류를 전체조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 001] 슬롯 전체조회 성공",
                            content = @Content(schema = @Schema(implementation = GetSlotListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetSlotListResponseDto> getSlotList() {
        return ResponseEntity.status(HttpStatus.OK).body(slotService.getSlotList());
    }

    @GetMapping("/accounts/{accountId}/slots")
    @Operation(
            summary = "5-1-2 계좌 슬롯 리스트 전체 조회",
            description = "계좌의 슬롯 리스트를 전체 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 015] 계좌의 슬롯 리스트 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetAccountSlotListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetAccountSlotListResponseDto> getAccountSlotList(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(slotService.getAccountSlotList(principal.userId(), accountId));
    }

    @PostMapping("/accounts/{accountId}/slots")
    @Operation(
            summary = "5-1-3 슬롯 추가등록",
            description = "서비스 이용 중 새로운 슬롯을 추가로 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "[SlotService - 035] 슬롯 추가 성공",
                            content = @Content(schema = @Schema(implementation = AddSlotListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<AddSlotListResponseDto> addSlots(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @RequestBody AddSlotListRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.addSlots(principal.userId(), accountId, request.getSlots()));
    }

    @PatchMapping("/accounts/{accountId}/slots/{accountSlotId}")
    @Operation(
            summary = "5-1-4 슬롯 정보수정",
            description = "슬롯에 별칭을 지정하거나 예산을 변경합니다. 별칭은 추후 같은 API에 customName 키 값을 default로 해서 요청을 보내시면 기본값으로 되돌아갑니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 006] 슬롯 정보수정 성공",
                            content = @Content(schema = @Schema(implementation = ModifyAccountSlotResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ModifyAccountSlotResponseDto> modifyAccountSlot(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String accountSlotId, @RequestBody ModifyAccountSlotRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(slotService.modifyAccountSlot(principal.userId(), accountId, accountSlotId, request));
    }

    @DeleteMapping("/accounts/{accountId}/slots/{accountSlotId}")
    @Operation(
            summary = "5-1-5 슬롯 삭제",
            description = "슬롯을 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "[SlotService - 011] 슬롯 삭제 성공",
                            content = @Content(schema = @Schema(implementation = RemoveAccountSlotResponseDto.class))
                    )
            }
    )
    public ResponseEntity<RemoveAccountSlotResponseDto> removeAccountSlot(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String accountSlotId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(slotService.removeAccountSlot(principal.userId(), accountId, accountSlotId));
    }

    @GetMapping("/accounts/{accountId}/slots/{accountSlotId}/history")
    @Operation(
            summary = "5-1-6 슬롯 히스토리 전체조회",
            description = "슬롯 예산변경 히스토리를 전체 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 020] 슬롯 히스토리 전체조회 성공",
                            content = @Content(schema = @Schema(implementation = GetSlotHistoryResponseDto.class))
                    )
            }
    )
    public ResponseEntity<GetSlotHistoryResponseDto> getSlotHistory(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @PathVariable String accountSlotId) {
        return ResponseEntity.status(HttpStatus.OK).body(slotService.getSlotHistory(principal.userId(), accountId, accountSlotId));
    }

    @PostMapping("/accounts/{accountId}/slots/recommend")
    @Operation(
            summary = "5-2-1 거래내역 기반 계좌 슬롯 리스트 추천",
            description = "주어진 기간 동안의 거래내역을 기반으로 계좌 슬롯 리스트를 추천합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 028] 슬롯 추천 성공",
                            content = @Content(schema = @Schema(implementation = RecommendSlotsResponseDto.class))
                    )
            }
    )
    public ResponseEntity<RecommendSlotsResponseDto> recommendSlots(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @RequestBody RecommendSlotsRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(slotService.recommendSlots(principal.userId(), accountId, request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/accounts/{accountId}/slots/recommend/by-profile")
    @Operation(
            summary = "5-2-2 사용자 정보 기반 계좌 슬롯 리스트 추천",
            description = "주어진 사용자 정보를 기반으로 슬롯 리스트를 추천합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "[SlotService - 028] 슬롯 추천 성공",
                            content = @Content(schema = @Schema(implementation = RecommendSlotsByProfileResponseDto.class))
                    )
            }
    )
    public ResponseEntity<RecommendSlotsByProfileResponseDto> recommendSlotsByProfile(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @RequestBody RecommendSlotsByProfileRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(slotService.recommendSlotsByProfile(principal.userId(), accountId, request));
    }

    @PatchMapping("/accounts/{accountId}/slots/reassign")
    @Operation(
            summary = "5-3-1 슬롯편성",
            description = "서비스에 계좌 연동 후 처음으로 슬롯 리스트를 확정하거나, 기준일이 도래하여 새로운 슬롯 리스트를 확정합니다. 확정하고자 하는 슬롯 리스트를 요청 바디에 넣어서 보내시면 됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "[SlotService - 040] 다음달 슬롯 리스트 등록 성공",
                            content = @Content(schema = @Schema(implementation = ModifyAccountSlotListResponseDto.class))
                    )
            }
    )
    public ResponseEntity<ModifyAccountSlotListResponseDto> modifyAccountSlots(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String accountId, @RequestBody ModifyAccountSlotListRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.modifyAccountSlots(principal.userId(), accountId, request.getSlots()));
    }
}
