package com.ssafy.b108.walletslot.backend.global.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PepperedPasswordEncoder
 *
 * - BCrypt(내장 Salt) 앞단에 pepper를 섞습니다: encode(peppper + raw).
 * - @Primary: 동일 타입(PasswordEncoder) 주입 충돌 시 이 빈이 우선 선택됩니다.
 */
@Primary
@Component
@RequiredArgsConstructor
public class PepperedPasswordEncoder implements PasswordEncoder {

    private final PepperManager peppers;
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        // 저장 시: bcrypt( pepper + raw )
        return delegate.encode(peppers.getCurrent() + rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 로그인 검증 시: 현재 pepper로 우선 매칭
        if (delegate.matches(peppers.getCurrent() + rawPassword, encodedPassword)) return true;
        // (선택) 이전 pepper도 허용(그레이스 기간)
        return peppers.getPrevious()
                .map(prev -> delegate.matches(prev + rawPassword, encodedPassword))
                .orElse(false);
    }
}
