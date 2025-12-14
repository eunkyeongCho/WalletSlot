package com.ssafy.b108.walletslot.backend.global.crypto;

import com.ssafy.b108.walletslot.backend.domain.auth.entity.PepperKey;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.UserPin;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.PepperKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUpgrader {

    private final PepperKeyRepository pepperKeyRepository;
    private final PepperSecretProvider pepperSecrets;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public void upgradeIfNeeded(UserPin pin, String rawPin, int targetCost) {
        // 스키마와 일치: ACTIVATE
        var activeOpt = pepperKeyRepository
                .findFirstByStatusOrderByCreatedAtDesc(PepperKey.Status.ACTIVATE);

        if (activeOpt.isEmpty()) return;
        PepperKey active = activeOpt.get();

        boolean needPepperRotate = !pin.getPepperKey().getId().equals(active.getId());
        boolean needCostRaise    = pin.getCost() < targetCost;
        if (!needPepperRotate && !needCostRaise) return;

        String newSecret = pepperSecrets.getSecret(active.getKeyAlias());
        if (newSecret == null) return;

        pin.upgrade(
                rawPin,
                active,
                newSecret,
                targetCost,
                bcrypt,
                java.time.Instant.now()
        );
    }
}
