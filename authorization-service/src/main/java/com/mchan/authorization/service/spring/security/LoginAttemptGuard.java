package com.mchan.authorization.service.spring.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.function.LongSupplier;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

/**
 * Bounds expensive human-password work and throttles repeated failures for a pseudonymous identity.
 */
@Component
public class LoginAttemptGuard {
    private final LinkedHashMap<String, Attempt> attempts = new LinkedHashMap<>();
    private final Semaphore hashing;
    private final LongSupplier clock;
    private long lastSweep;

    /**
     * Sets the initial single-instance budget; user passwords always retain their BCrypt cost.
     */
    public LoginAttemptGuard() {
        this(8, System::nanoTime);
    }

    /**
     * Provides deterministic bounded concurrency and time for security regression tests.
     */
    public LoginAttemptGuard(int maximumHashing, LongSupplier clock) {
        if (maximumHashing < 1 || maximumHashing > 32) {
            throw new IllegalArgumentException("Human password concurrency is outside bounds");
        }
        this.hashing = new Semaphore(maximumHashing);
        this.clock = clock;
        this.lastSweep = clock.getAsLong();
    }

    /**
     * Returns an owned hashing slot or rejects before database/password work.
     */
    public synchronized void enter(String username, String password) {
        if (username == null || username.isBlank() || username.length() > 254 || password == null
            || password.isEmpty() || password.length() > 128) {
            throw new BadCredentialsException("Invalid username or password");
        }
        long now = clock.getAsLong();
        String key = key(username);
        Attempt attempt = attempts.get(key);
        if (attempt != null && now - attempt.lastFailure < 60000000000L && attempt.failures >= 5) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (attempts.size() >= 10000 && attempt == null) {
            if (now - lastSweep >= 1000000000L) {
                attempts.entrySet().removeIf(entry -> now - entry.getValue().lastFailure >= 60000000000L);
                lastSweep = now;
            }
            if (attempts.size() >= 10000) {
                throw new AuthenticationServiceException("Authentication temporarily unavailable");
            }
        }
        if (!hashing.tryAcquire()) {
            throw new AuthenticationServiceException("Authentication temporarily unavailable");
        }
    }

    /**
     * Records a bounded failure state without storing the supplied password or clear username.
     */
    public synchronized void failed(String username) {
        long now = clock.getAsLong();
        String key = key(username);
        Attempt attempt = attempts.get(key);
        if (attempt == null || now - attempt.lastFailure >= 60000000000L) {
            if (attempts.size() >= 10000 && attempt == null) {
                return;
            }
            attempt = new Attempt();
            attempts.put(key, attempt);
        }
        attempt.failures++;
        attempt.lastFailure = now;
    }

    /**
     * Clears the identity's failure state after a valid authentication.
     */
    public synchronized void succeeded(String username) {
        attempts.remove(key(username));
    }

    /**
     * Releases only a slot acquired for the current authentication attempt.
     */
    public void release() {
        hashing.release();
    }

    private String key(String username) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(username.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Identity fingerprint unavailable", e);
        }
    }

    private static final class Attempt {
        private int failures;
        private long lastFailure;
    }
}
