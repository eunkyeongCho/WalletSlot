package com.ssafy.b108.walletslot.backend.infrastructure.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(SmsSender.class)
public class ConsoleSmsSender implements SmsSender {
    @Override
    public boolean sendText(String to, String text) {
        log.warn("[DEV-SMS] to={}, text={}", to, text);
        return true;
    }
}
