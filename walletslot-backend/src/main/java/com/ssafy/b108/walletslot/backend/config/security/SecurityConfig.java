package com.ssafy.b108.walletslot.backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final DeviceBindingFilter deviceBindingFilter;

    /* ---------------------- 운영/개발용 (test 제외) ---------------------- */
    @Bean
    @Profile("!test")
    public SecurityFilterChain appSecurityChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // CORS 적용
                .cors(c -> c.configurationSource(corsConfigurationSource()))

                // 기본 로그인/베이직 인증 비활성화
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())

                // 세션 정책 설정
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Swagger & 문서
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/api-docs/**",
                                "/api-docs",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/webjars/**"
                        ).permitAll()

                        // devController를 위한
                        .requestMatchers("/api/dev/**").permitAll()

                        // 1원 인증: 회원가입 전 공개
                        .requestMatchers("/api/accounts/verification/**").permitAll()

                        // 인증 필요한 API
                        .requestMatchers("/api/auth/me").authenticated()

                        // ocr을 위한
                        .requestMatchers("/api/ocr/**").authenticated()

                        // 헬스체크 & 인증 엔드포인트
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/**",
                                "/api/auth/**",
                                "/api/ping/public"
                        ).permitAll()

                        // CORS Preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyRequest().authenticated()
                )

                // 401/403 JSON 응답 표준화
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jsonAuthEntryPoint())
                        .accessDeniedHandler(jsonAccessDeniedHandler())
                )

                // JWT 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 디바이스 바인딩 검증 (JWT 이후)
                .addFilterAfter(deviceBindingFilter, JwtAuthFilter.class);

        return http.build();
    }

    /* ---------------------- 테스트용 (전부 허용) ---------------------- */
    @Bean
    @Profile("test")
    public SecurityFilterChain testSecurityChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /* ---------------------- 공통 Bean ---------------------- */

    // 401 JSON
    @Bean
    public AuthenticationEntryPoint jsonAuthEntryPoint() {
        return (req, res, ex) -> {
            res.setStatus(401);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
        };
    }

    // 403 JSON
    @Bean
    public AccessDeniedHandler jsonAccessDeniedHandler() {
        return (req, res, ex) -> {
            res.setStatus(403);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"접근 권한이 없습니다.\"}");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // CORS 정책
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOriginPatterns(List.of("*"));
        /*
        ===== 배포 시 이걸로 교체 =====
        // React Native는 CORS 제약이 없지만, Swagger/웹 호출 대비 허용 패턴 정리
        // - 에뮬레이터(안드로이드): http://10.0.2.2:{port}
        // - 로컬/사내망 테스트: localhost/127.0.0.1/192.168.x.x
        // - 운영에서는 "정확한 도메인 화이트리스트"로 교체 권장
        c.setAllowedOriginPatterns(List.of(
                "https://j13b108.p.ssafy.io",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://10.0.2.2:*",
                "http://10.0.3.2:*",      // Genymotion
                "http://192.168.*.*:*"
        ));
         */

        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Device-Id",
                "X-Requested-With"
        ));
        c.setExposedHeaders(List.of("Authorization"));
        // Bearer 헤더 기반이므로 크리덴셜은 불필요(쿠키 미사용)
        c.setAllowCredentials(false);
        // 프리플라이트 캐시 시간(초)
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
