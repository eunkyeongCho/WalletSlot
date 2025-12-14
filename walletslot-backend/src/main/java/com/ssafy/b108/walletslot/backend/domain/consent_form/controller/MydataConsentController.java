package com.ssafy.b108.walletslot.backend.domain.consent_form.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.consent_form.dto.*;
import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent;
import com.ssafy.b108.walletslot.backend.domain.consent_form.service.MydataConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mydata/consents")
@RequiredArgsConstructor
@Tag(name = "MyData Consents")
public class MydataConsentController {

    private final MydataConsentService service;

    @PostMapping
    @Operation(summary = "3-1 통합 동의 생성+확정",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "1")))
    public ResponseEntity<ConsentCreateResponseDto> create(
            @AuthenticationPrincipal final UserPrincipal principal,
            @RequestBody @Valid final ConsentCreateRequestDto req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(principal.userId(), req));
    }

    @GetMapping
    @Operation(summary = "3-2 동의 상태 조회 (status 옵션: ACTIVE | EXPIRED | REVOKED)",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "2")))
    public ResponseEntity<ConsentListResponseDto> list(
            @AuthenticationPrincipal final UserPrincipal principal,
            @RequestParam(required = false) final UserConsent.Status status
    ) {
        return ResponseEntity.ok(service.list(principal.userId(), status));
    }

    @PostMapping("/revoke")
    @Operation(summary = "3-3 동의 철회",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "3")))
    public ResponseEntity<ConsentRevokeResponseDto> revoke(
            @AuthenticationPrincipal final UserPrincipal principal,
            @RequestBody @Valid final ConsentRevokeRequestDto req
    ) {
        return ResponseEntity.ok(service.revoke(principal.userId(), req));
    }

    @PostMapping("/renew")
    @Operation(summary = "3-4 동의 재동의/연장",
            description = "previousUserConsentUuid가 있으면 해당 건 기준 EXPIRED 처리 후 새 동의 생성. 없으면 consentFormUuid로 새 동의 생성.",
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "order", value = "4")))
    public ResponseEntity<ConsentRenewResponseDto> renew(
            @AuthenticationPrincipal final UserPrincipal principal,
            @RequestBody @Valid final ConsentRenewRequestDto req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.renew(principal.userId(), req));
    }
}
