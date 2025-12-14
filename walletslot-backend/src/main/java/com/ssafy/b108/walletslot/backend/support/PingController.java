package com.ssafy.b108.walletslot.backend.support;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;   // ✅ 이거!
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping("/public")
    public Map<String, String> publicPing() {
        return Map.of("pong", "public");
    }

    // 🔒 Swagger가 Bearer + X-Device-Id 를 자동으로 붙이도록 지정
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "deviceId")
    @GetMapping("/protected")
    public Map<String, String> protectedPing(Authentication auth) {
        // JwtProvider.getAuthentication()에서 principal을 "user:<sub>"로 넣었으니 그대로 볼 수 있음
        String principal = (auth == null || auth.getPrincipal() == null) ? "unknown" : auth.getPrincipal().toString();
        return Map.of("pong", "ok", "principal", principal);
    }
}
