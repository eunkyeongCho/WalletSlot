package com.ssafy.b108.walletslot.backend.infrastructure.ssafy;

public interface SsafyMemberClient {

    /** 회원 조회: 있으면 userKey 반환, 없으면 null 또는 예외 처리 */
    String searchUserKeyByEmail(String email);

    /** 회원 생성: 성공 시 userKey 반환 */
    String createMemberAndGetUserKey(String email);

    /** 조회 → 없으면 생성 → 최종 userKey 반환 */
    String getOrCreateUserKeyByEmail(String email);
}
