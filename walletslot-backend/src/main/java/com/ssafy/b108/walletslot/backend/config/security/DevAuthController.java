package com.ssafy.b108.walletslot.backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final JwtProvider jwtProvider;

    public record AccessToken(String accessToken) {}

    /** 예: POST /api/dev/login-as/1  (헤더: X-Device-Id: DEV-EMUL-001) */
    @PostMapping("/login-as/{userId}")
    public AccessToken loginAs(@PathVariable Long userId,
                               @RequestHeader("X-Device-Id") String deviceId) {
        return new AccessToken(jwtProvider.createAccessToken(userId, deviceId));
    }
}
