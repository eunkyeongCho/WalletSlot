package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SSAFYGetUserKeyResponseDto {

    String userKey; // 응답 JSON이 {"userKey":"..."} 형태라고 가정
}
