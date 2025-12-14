package com.ssafy.b108.walletslot.backend.domain.user.service;

public interface VerificationService {
    void assertPhoneToken(long userId, String phoneNumber, String token);
    void assertEmailToken(long userId, String email, String token);
}
