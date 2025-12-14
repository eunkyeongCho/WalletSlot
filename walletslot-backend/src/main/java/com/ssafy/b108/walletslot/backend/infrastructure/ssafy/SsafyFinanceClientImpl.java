package com.ssafy.b108.walletslot.backend.infrastructure.ssafy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.b108.walletslot.backend.domain.account.dto.external.SSAFYGetUserKeyResponseDto;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SsafyFinanceClientImpl implements SsafyFinanceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${api.ssafy.finance.apiKey}")
    private String ssafyFinanceApiKey;

    @Value("${api.ssafy.finance.baseUrl:https://finopenapi.ssafy.io}")
    private String ssafyBaseUrl; // 예: https://finopenapi.ssafy.io

    /**
     * SSAFY 회원 조회: userId(email)로 userKey 조회.
     * 존재하지 않으면 null 반환(문서상 E4003).
     */
    @Override
    public String getUserKeyByEmail(String email) {
        final String url = ssafyBaseUrl + "/ssafy/api/v1/member/search";

        // 문서대로 단순 JSON 바디
        Map<String, Object> body = new HashMap<>();
        body.put("apiKey", ssafyFinanceApiKey);
        body.put("userId", email);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body);

        try {
            ResponseEntity<SSAFYGetUserKeyResponseDto> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SSAFYGetUserKeyResponseDto.class
            );

            return (resp.getBody() != null) ? resp.getBody().getUserKey() : null;

        } catch (HttpStatusCodeException ex) {
            // E4003: 등록되지 않은 ID → null 반환
            String code = extractErrorCode(ex);
            if ("E4003".equalsIgnoreCase(code)) {
                return null;
            }
            // 그 외는 게이트웨이 에러로 래핑
            throw new AppException(
                    ErrorCode.BAD_REQUEST,
                    "[SsafyFinanceClient - search] SSAFY API error: "
                            + ex.getStatusCode() + " - " + ex.getResponseBodyAsString()
            );
        }
    }

    // 에러 바디에서 "code"만 뽑아오는 간단 파서(구조가 단순하니 Map으로 파싱)
    private String extractErrorCode(HttpStatusCodeException ex) {
        try {
            Map<?, ?> m = objectMapper.readValue(ex.getResponseBodyAsString(), Map.class);
            Object code = m.get("code");
            return code != null ? code.toString() : null;
        } catch (Exception ignore) {
            return null;
        }
    }
}
