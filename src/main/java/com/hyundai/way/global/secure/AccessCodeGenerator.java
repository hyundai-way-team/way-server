package com.hyundai.way.global.secure;

import java.security.SecureRandom;
import java.util.Base64;

public class AccessCodeGenerator {

    private static final int ACCESS_CODE_LENGTH = 32;

    private AccessCodeGenerator() {}

    public static String issue(SecureRandom random) {
        byte[] bytes = new byte[ACCESS_CODE_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
