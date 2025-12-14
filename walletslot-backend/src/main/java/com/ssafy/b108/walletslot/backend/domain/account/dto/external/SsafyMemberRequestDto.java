package com.ssafy.b108.walletslot.backend.domain.account.dto.external;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SsafyMemberRequestDto {
    String apiKey;   // SSAFY 개발센터에서 받은 키
    String userId;   // 이메일 형식
}
