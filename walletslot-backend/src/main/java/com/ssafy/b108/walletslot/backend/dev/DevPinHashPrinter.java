package com.ssafy.b108.walletslot.backend.dev;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev") // dev에서만 활성화
public class DevPinHashPrinter {

    @Bean
    public PasswordEncoder devPasswordEncoder() {
        return new BCryptPasswordEncoder(10); // DB의 cost와 일치
    }

    @Bean
    public ApplicationRunner printDevPinHash(PasswordEncoder pe) {
        return args -> {
            String secret = "dev-secret";  // application-dev.yml 의 pepper_v1이 plain:dev-secret 일 때
            // ✅ 서버 검증과 동일한 결합 규칙: secret + pin (pepper + pin)
            String raw1 = secret + "123456"; // 사용자1 PIN 1234
            String raw2 = secret + "4321"; // 사용자2 PIN 4321 (원하면)

            System.out.println("USER1_HASH = " + pe.encode(raw1));
            System.out.println("USER2_HASH = " + pe.encode(raw2));
        };
    }
}
