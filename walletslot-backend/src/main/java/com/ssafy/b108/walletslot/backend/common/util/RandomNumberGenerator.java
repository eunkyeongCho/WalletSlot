package com.ssafy.b108.walletslot.backend.common.util;

import java.security.SecureRandom;

public class RandomNumberGenerator {

    // Method
    public static String generateRandomNumber() {
        int num = new SecureRandom().nextInt(1_000_000);
        return String.format("%06d", num);
    }
}
