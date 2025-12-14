package com.ssafy.b108.walletslot.backend.domain.consent_form.service;

import com.ssafy.b108.walletslot.backend.domain.consent_form.dto.*;

import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent;

public interface MydataConsentService {

    ConsentCreateResponseDto create(Long userId, ConsentCreateRequestDto req);

    ConsentListResponseDto list(Long userId, UserConsent.Status status);

    ConsentRevokeResponseDto revoke(Long userId, ConsentRevokeRequestDto req);

    ConsentRenewResponseDto renew(Long userId, ConsentRenewRequestDto req);
}
