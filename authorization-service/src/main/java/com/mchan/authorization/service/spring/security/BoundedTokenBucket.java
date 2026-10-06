package com.mchan.authorization.service.spring.security;

import java.util.LinkedHashMap;
import java.util.function.LongSupplier;

/**
 * Strictly bounded per-instance admission state with monotonic token refill.
 */
public class BoundedTokenBucket {
    private final int maximumKeys;
    private final LongSupplier clock;
    private long lastSweep;
    private final LinkedHashMap<String, Bucket> buckets = new LinkedHashMap<>(16, 0.75f, true);

    /**
     * Creates bounded key state with an injectable monotonic clock.
     */
    public BoundedTokenBucket(int maximumKeys, LongSupplier clock) {
        if (maximumKeys < 1 || maximumKeys > 100000) {
            throw new IllegalArgumentException("Rate-limit key capacity is outside bounds");
        }
        this.maximumKeys = maximumKeys;
        this.clock = clock;
        this.lastSweep = clock.getAsLong();
    }

    /**
     * Admits one request or rejects it without allocating unbounded state.
     */
    public synchronized boolean allow(String key, int perSecond, int burst) {
        if (key == null || key.length() > 512 || perSecond < 1 || burst < 1) {
            throw new IllegalArgumentException("Invalid rate-limit policy");
        }
        long now = clock.getAsLong();
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            // Reject new keys at capacity instead of evicting active limits that attackers could reset.
            if (buckets.size() >= maximumKeys) {
                if (now - lastSweep >= 1000000000L) {
                    buckets.entrySet().removeIf(entry -> now - entry.getValue().lastSeen > 60000000000L);
                    lastSweep = now;
                }
                if (buckets.size() >= maximumKeys) {
                    return false;
                }
            }
            bucket = new Bucket(burst, now);
            buckets.put(key, bucket);
        }
        bucket.tokens = Math.min(burst, bucket.tokens + Math.max(0, now - bucket.lastSeen) / 1000000000.0 * perSecond);
        bucket.lastSeen = now;
        if (bucket.tokens < 1) {
            return false;
        }
        bucket.tokens -= 1;
        return true;
    }

    /**
     * Exposes the state bound for operational tests without recording keys or credentials.
     */
    public synchronized int size() {
        return buckets.size();
    }

    private static final class Bucket {
        private double tokens;
        private long lastSeen;

        private Bucket(double tokens, long now) {
            this.tokens = tokens;
            this.lastSeen = now;
        }
    }
}
