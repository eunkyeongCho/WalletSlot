package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SsafyMemberResponseDto {
    String userId;
    String userName;
    String institutionCode;
    String userKey;
    String created;   // 문자열로만 사용(문서도 String 명시)
    String modified;  // 문자열로만 사용
}
