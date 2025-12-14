package com.ssafy.b108.walletslot.backend.domain.user.service;

import org.springframework.stereotype.Service;

/**
 * 임시(개발용) 구현: 실제 검증은 나중에 9-2 모듈 연동 시 교체.
 * 지금은 빈 주입만 해결하기 위해 통과시킵니다.
 */
@Service
public class VerificationServiceImpl implements VerificationService {

    @Override
    public void assertPhoneToken(long userId, String phoneNumber, String token) {
        // TODO: 9-2-2 확인 API와 연계해 토큰 검증/만료 확인
        // 개발 중엔 No-Op (필요하면 아래 주석 해제해 간단히 체크)
        // if (token == null || token.isBlank()) throw new IllegalArgumentException("phoneVerificationToken required");
    }

    @Override
    public void assertEmailToken(long userId, String email, String token) {
        // TODO: 9-2-4 확인 API와 연계해 토큰 검증/만료 확인
        // if (token == null || token.isBlank()) throw new IllegalArgumentException("emailVerificationToken required");
    }
}
