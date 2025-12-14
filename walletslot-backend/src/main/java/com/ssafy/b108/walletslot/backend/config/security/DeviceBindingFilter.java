package com.ssafy.b108.walletslot.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DeviceBindingFilter extends OncePerRequestFilter {

    private static final String DEVICE_HEADER = "X-Device-Id";
    private static final String AUTH_HEADER   = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String VERIFY_PREFIX = "/api/accounts/verification/"; // ✅ 추가

    // 공개 엔드포인트(디바이스 검사 제외)
    private static final Set<String> SKIP_PREFIX = Set.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/actuator/health",
            "/error",
            "/api/ping/public",
            "/api/dev"
    );

    private final JwtProvider jwtProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String uri = request.getServletPath(); // 컨텍스트 패스 제외

        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true; // CORS preflight
        if (!uri.startsWith("/api/")) return true;                        // 비-API 경로 제외

        // 1원 인증은 회원가입 전 공개 → 디바이스 바인딩 검사 제외
        if (uri.startsWith(VERIFY_PREFIX)) return true;

        // 스웨거/헬스 등 공개 프리픽스 제외
        return SKIP_PREFIX.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // JWT 인증 필터 이후(인증된 요청)에만 검사
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            chain.doFilter(req, res);
            return;
        }

        // 1) 헤더 did 필수
        String headerDid = trimOrNull(req.getHeader(DEVICE_HEADER));
        if (headerDid == null) {
            write401(res, "디바이스 ID 헤더(X-Device-Id)가 없습니다.");
            return;
        }

        // 2) 토큰 did
        String tokenDid = null;

        if (auth.getPrincipal() instanceof UserPrincipal p) {
            tokenDid = trimOrNull(p.deviceId());
        }
        if (tokenDid == null && auth instanceof AbstractAuthenticationToken aat) {
            Object d = aat.getDetails();
            if (d instanceof String s) tokenDid = trimOrNull(s);
        }
        if (tokenDid == null) {
            String authz = req.getHeader(AUTH_HEADER);
            String token = (authz != null && authz.startsWith(BEARER_PREFIX))
                    ? authz.substring(BEARER_PREFIX.length()) : null;
            if (token == null) {
                write401(res, "인증 토큰이 없습니다.");
                return;
            }
            tokenDid = trimOrNull(jwtProvider.extractDeviceId(token));
            if (tokenDid == null) {
                write401(res, "토큰에 디바이스 정보가 없습니다.");
                return;
            }
        }

        // 3) 일치 검사
        if (!Objects.equals(headerDid, tokenDid)) {
            write401(res, "현재 토큰의 디바이스와 일치하지 않습니다.");
            return;
        }

        chain.doFilter(req, res);
    }

    /* ------------------------ helpers ------------------------ */
    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void write401(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
