package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.domain.auth.dto.Tokens;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "app.security.refresh.enabled", havingValue = "false", matchIfMissing = true)
public class RefreshTokenServiceDisabled implements RefreshTokenService {

    @Override public String issueAfterLogin(Long userId, String deviceId) { return null; }

    @Override public Tokens rotate(String oldRefresh, String deviceId) {
        throw new AppException(ErrorCode.REFRESH_DISABLED, "Refresh 비활성화");
    }

    @Override public void revoke(String refresh, String deviceId) {
        throw new AppException(ErrorCode.REFRESH_DISABLED, "Refresh 비활성화");
    }
}
