package com.ssafy.b108.walletslot.backend.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * SSAFY 금융망 API에 요청보낼 때 항상 포함시켜야 하는 Body 입니다.
 * Body에 들어가는 key 중 하나의 이름을 "Header"로 해서 아래의 내용을 포함해야 합니다. (불가피하게 camelCase가 아닌 PascalCase로 key명 작성)
 */
public class Header {

    // Field
    private String apiName; // 호출 API의 마지막 path명
    private String transmissionDate; // API 전송일자 (yyyyMMdd)
    private String transmissionTime; // API 전송시각 (hhmmss)

    @Builder.Default
    private String institutionCode = "00100"; // 00100으로 고정

    @Builder.Default
    private String fintechAppNo = "001"; // 001로 고정

    private String apiServiceCode; // apiName 필드와 값 동일
    private String institutionTransactionUniqueNo; // API 호출 고유번호 (yyyyMMddhhmmss+임의 생성한 일련번호 6자리)
    private String apiKey;
    private String userKey;
}
