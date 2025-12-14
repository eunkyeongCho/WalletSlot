package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.global.error.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneVerifyTicketService {
    private static class Ticket {
        final String phone,purpose;
        final Instant exp;
        Ticket(String p,String pu,Instant e){
            phone=p;purpose=pu;exp=e;
        }
    }

    private final Map<String, Ticket> store = new ConcurrentHashMap<>();
    private static final long TTL_SECONDS = 600;

    public String issue(String phone, String purpose) {
        String t = UUID.randomUUID().toString();
        store.put(t, new Ticket(phone, purpose, Instant.now().plusSeconds(TTL_SECONDS)));
        return t;
    }

    /** 최종 가입에서 소모 */
    public void consume(String ticket, String expectedPurpose, String expectedPhone) {
        Ticket tk = store.remove(ticket);
        if (tk == null) throw new AppException(ErrorCode.FORBIDDEN, "유효하지 않은 티켓");
        if (!tk.purpose.equals(expectedPurpose)) throw new AppException(ErrorCode.FORBIDDEN, "목적 불일치");
        if (!tk.phone.equals(expectedPhone)) throw new AppException(ErrorCode.FORBIDDEN, "전화번호 불일치");
        if (Instant.now().isAfter(tk.exp)) throw new AppException(ErrorCode.OTP_EXPIRED, "티켓 만료");
    }
}
