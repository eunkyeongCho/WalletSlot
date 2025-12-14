package com.ssafy.b108.walletslot.backend.global.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PepperSecretProvider {

    private static final String PREFIX_PLAIN = "plain:";
    private static final Bindable<Map<String, String>> MAP_STRING_STRING =
            Bindable.mapOf(String.class, String.class);

    private final Optional<KmsClient> kms;         // dev 에서는 없어도 됨
    private final Map<String, String> cipherMap;   // alias -> (plain:... | KMS-Base64)
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public PepperSecretProvider(
            @Nullable KmsClient kms,
            Environment env
    ) {
        this.kms = Optional.ofNullable(kms);
        // ❗ Environment Binder로 안전하게 읽기 (없으면 빈 맵)
        this.cipherMap = Binder.get(env)
                .bind("app.security.pepper.cipher", MAP_STRING_STRING)
                .orElseGet(Collections::emptyMap);

        if (this.cipherMap.isEmpty()) {
            log.warn("pepper cipher map is EMPTY. Put entries under 'app.security.pepper.cipher.<alias>' in your yml.");
        } else {
            log.debug("pepper cipher aliases loaded: {}", this.cipherMap.keySet());
        }
    }

    public String getSecret(String alias) {
        return cache.computeIfAbsent(alias, this::resolveSecret);
    }

    private String resolveSecret(String alias) {
        String configured = cipherMap.get(alias);
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("Unknown pepper alias: " + alias);
        }

        // dev: plain:secret
        if (configured.startsWith(PREFIX_PLAIN)) {
            String plain = configured.substring(PREFIX_PLAIN.length());
            if (plain.isBlank()) {
                throw new IllegalStateException("Empty plain secret for alias: " + alias);
            }
            log.debug("pepper '{}' resolved via plain: (dev)", alias);
            return plain;
        }

        // prod: KMS 복호화
        KmsClient kmsClient = kms.orElseThrow(() ->
                new IllegalStateException("KMS client not configured but ciphertext provided for alias: " + alias));

        byte[] cipherBytes;
        try {
            cipherBytes = Base64.getDecoder().decode(configured);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid Base64 ciphertext for alias: " + alias, e);
        }

        var resp = kmsClient.decrypt(DecryptRequest.builder()
                .ciphertextBlob(SdkBytes.fromByteArray(cipherBytes))
                .build());
        String plaintext = resp.plaintext().asUtf8String();
        if (plaintext == null || plaintext.isBlank()) {
            throw new IllegalStateException("Decrypted empty secret for alias: " + alias);
        }
        log.debug("pepper '{}' resolved via KMS decrypt", alias);
        return plaintext;
    }
}
