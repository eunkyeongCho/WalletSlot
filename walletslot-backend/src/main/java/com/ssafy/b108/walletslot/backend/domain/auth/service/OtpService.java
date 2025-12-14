package com.ssafy.b108.walletslot.backend.domain.auth.service;

public interface OtpService {
    void send(String phone, String purpose, String deviceId);
    boolean verify(String phone, String purpose, String code);

    // DEVICE_VERIFY용 오버로드(단말 키 분리)
    default boolean verify(String phone, String purpose, String code, String deviceId) {
        return verify(phone, purpose, code);
    }
}
