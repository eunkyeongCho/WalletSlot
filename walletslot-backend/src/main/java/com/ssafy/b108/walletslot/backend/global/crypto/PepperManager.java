package com.ssafy.b108.walletslot.backend.global.crypto;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * PepperManager
 *
 * - 현재 pepper와 (선택) 이전 pepper를 관리합니다.
 * - 이전 pepper는 "그레이스 기간(유예기간)" 동안만 허용하고,
 *   로그인 성공 시 최신 pepper로 재해시(업그레이드)합니다.
 */
@Getter
@Component
public class PepperManager {

    private final String current;             // 현재 운영 pepper (필수)
    private final Optional<String> previous;  // 이전 pepper (선택)

    public PepperManager(
            @Value("${app.security.pepper.current:}") String current,
            @Value("${app.security.pepper.previous:}") String previous
    ) {
        this.current  = current == null ? "" : current;
        this.previous = Optional.ofNullable(previous).filter(p -> !p.isBlank());
    }
}
