package com.mchan.authorization.service.authorization.components;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Fingerprints only server-generated 256-bit client secrets; human passwords retain BCrypt.
 */
@Component
public class ClientSecretHasher {
    private static final String PREFIX = "hmac-sha256$";
    private final byte[] key;

    /**
     * Uses the externally configured pepper for a versioned, one-way client-secret fingerprint.
     */
    public ClientSecretHasher(@Qualifier("pepperValue") String pepper) {
        if (pepper == null || pepper.isBlank()) {
            throw new IllegalArgumentException("A client-secret pepper is required");
        }
        this.key = pepper.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Hashes a uniformly random secret without applying the human-password KDF to every request.
     */
    public String hash(String secret) {
        if (secret == null || !secret.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException("Expected a generated 256-bit client secret");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return PREFIX + HexFormat.of().formatHex(mac.doFinal(secret.getBytes(StandardCharsets.US_ASCII)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Unable to fingerprint client credentials", e);
        }
    }

    /**
     * Checks the stored fingerprint with a constant-time digest comparison.
     */
    public boolean matches(String secret, String stored) {
        return stored != null && stored.startsWith(PREFIX) && MessageDigest.isEqual(
            hash(secret).getBytes(StandardCharsets.US_ASCII), stored.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Identifies supported versioned fingerprints while preserving existing BCrypt credentials.
     */
    public boolean isCurrent(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }
}
