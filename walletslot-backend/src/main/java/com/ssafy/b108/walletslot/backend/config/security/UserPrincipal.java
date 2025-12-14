package com.ssafy.b108.walletslot.backend.config.security;

import java.io.Serializable;

/** SecurityContext에 넣어 줄 사용자 정보 */
public record UserPrincipal(Long userId, String deviceId) implements Serializable {}
