package com.ssafy.b108.walletslot.backend.infrastructure.ssafy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.b108.walletslot.backend.domain.account.dto.external.SsafyErrorResponseDto;
import com.ssafy.b108.walletslot.backend.domain.account.dto.external.SsafyMemberRequestDto;
import com.ssafy.b108.walletslot.backend.domain.account.dto.external.SsafyMemberResponseDto;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import com.ssafy.b108.walletslot.backend.infrastructure.ssafy.SsafyMemberClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SsafyMemberClientImpl implements SsafyMemberClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.ssafy.finance.apiKey}")
    private String apiKey;

    @Value("${api.ssafy.finance.baseUrl:https://finopenapi.ssafy.io}")
    private String baseUrl;

    // POST /ssafy/api/v1/member/
    private String createUrl() {
        return baseUrl + "/ssafy/api/v1/member/";
    }

    // POST /ssafy/api/v1/member/search
    private String searchUrl() {
        return baseUrl + "/ssafy/api/v1/member/search";
    }

    @Override
    public String searchUserKeyByEmail(String email) {
        SsafyMemberRequestDto body = SsafyMemberRequestDto.builder()
                .apiKey(apiKey)
                .userId(email)
                .build();

        HttpEntity<SsafyMemberRequestDto> entity = new HttpEntity<>(body);

        try {
            ResponseEntity<SsafyMemberResponseDto> resp = restTemplate.exchange(
                    searchUrl(),
                    HttpMethod.POST,
                    entity,
                    SsafyMemberResponseDto.class
            );
            SsafyMemberResponseDto dto = resp.getBody();
            return dto != null ? dto.getUserKey() : null;
        } catch (HttpStatusCodeException ex) {
            // 4003(없는 email) 등 처리: null 반환하여 상위에서 생성 로직으로
            SsafyErrorResponseDto err = parseError(ex);
            if (err != null && "E4003".equalsIgnoreCase(err.getCode())) {
                return null;
            }
            // 그 외 에러는 그대로 전파
            throw toAppException(ex, "[SsafyMemberClient - search]");
        }
    }

    @Override
    public String createMemberAndGetUserKey(String email) {
        SsafyMemberRequestDto body = SsafyMemberRequestDto.builder()
                .apiKey(apiKey)
                .userId(email)
                .build();

        HttpEntity<SsafyMemberRequestDto> entity = new HttpEntity<>(body);

        try {
            ResponseEntity<SsafyMemberResponseDto> resp = restTemplate.exchange(
                    createUrl(),
                    HttpMethod.POST,
                    entity,
                    SsafyMemberResponseDto.class
            );
            SsafyMemberResponseDto dto = resp.getBody();
            if (dto == null || dto.getUserKey() == null || dto.getUserKey().isBlank()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "[SsafyMemberClient - create] userKey empty");
            }
            return dto.getUserKey();
        } catch (HttpStatusCodeException ex) {
            // 이미 존재(E4002)면 조회 시도로 회귀
            SsafyErrorResponseDto err = parseError(ex);
            if (err != null && "E4002".equalsIgnoreCase(err.getCode())) {
                return searchUserKeyByEmail(email);
            }
            throw toAppException(ex, "[SsafyMemberClient - create]");
        }
    }

    @Override
    public String getOrCreateUserKeyByEmail(String email) {
        // 1) 먼저 조회
        String found = searchUserKeyByEmail(email);
        if (found != null && !found.isBlank()) return found;

        // 2) 없으면 생성
        return createMemberAndGetUserKey(email);
    }

    private SsafyErrorResponseDto parseError(HttpStatusCodeException ex) {
        try {
            String body = ex.getResponseBodyAsString();
            return objectMapper.readValue(body, SsafyErrorResponseDto.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private AppException toAppException(HttpStatusCodeException ex, String marker) {
        return new AppException(
                ErrorCode.BAD_REQUEST,
                marker + " SSAFY API error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString()
        );
    }
}
