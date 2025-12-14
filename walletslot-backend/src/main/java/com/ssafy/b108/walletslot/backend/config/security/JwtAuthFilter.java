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
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER   = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // 1원 인증 경로
    private static final String VERIFY_PREFIX = "/api/accounts/verification/";

    // 공개 프리픽스들(스웨거/헬스 등)
    private static final Set<String> WHITELIST_PREFIX = Set.of(
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS Preflight는 항상 패스
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        // 컨텍스트 패스 제외한 서블릿 경로로 비교 (프록시/서블릿 컨텍스트 대비 안전)
        final String path = request.getServletPath();

        // 1원 인증 경로는 필터 자체를 아예 타지 않게 스킵
        if (path.startsWith(VERIFY_PREFIX)) return true;

        // Swagger/헬스 등 공개 프리픽스도 스킵
        return isWhitelisted(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String token = resolveBearerToken(req);
        if (token != null && jwtProvider.validate(token)) {
            final Authentication auth = jwtProvider.getAuthentication(token);
            if (auth != null) {
                if (auth instanceof AbstractAuthenticationToken aat) {
                    String did = safelyExtractDid(token); // 실패해도 null 허용
                    aat.setDetails(did);                  // DeviceBindingFilter 보조용
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(req, res);
    }

    /* ------------------------ helpers ------------------------ */

    private boolean isWhitelisted(String path) {
        if (path == null) return false;
        return WHITELIST_PREFIX.stream().anyMatch(path::startsWith);
    }

    private String resolveBearerToken(HttpServletRequest req) {
        final String authz = req.getHeader(AUTH_HEADER);
        if (authz == null || !authz.startsWith(BEARER_PREFIX)) return null;
        return authz.substring(BEARER_PREFIX.length());
    }

    private String safelyExtractDid(String token) {
        try {
            return jwtProvider.extractDeviceId(token);
        } catch (Exception ignored) {
            return null;
        }
    }
}
