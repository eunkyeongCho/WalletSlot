package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.domain.auth.dto.Tokens;

public interface RefreshTokenService {
    String issueAfterLogin(Long userId, String deviceId);
    Tokens rotate(String oldRefresh, String deviceId);
    void revoke(String refresh, String deviceId);
}
