package com.ssafy.b108.walletslot.backend.infrastructure.sms;

public interface SmsSender {
    boolean sendText(String to, String text);
}
