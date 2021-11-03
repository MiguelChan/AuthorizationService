package com.mchan.authorization.service.entities.utils;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Generates a Random Id.
 */
@Component
public class RandomIdGenerator {

    private static final int MAX_DIGITS = 20;

    /**
     * Generates a random Id of {@link RandomIdGenerator#MAX_DIGITS}.
     *
     * @param prefix .
     * @return .
     */
    public String generateRandomId(String prefix) {
        return this.generateRandomId(prefix, MAX_DIGITS);
    }

    /**
     * Generates a randomId of {@param maxDigits}.
     *
     * @param prefix .
     *
     * @param maxDigits .
     *
     * @return .
     */
    public String generateRandomId(String prefix, int maxDigits) {
        String randomUuid = UUID.randomUUID().toString().replace("-", "");
        String formattedId = String.format("%s%s", prefix, randomUuid.toUpperCase());
        return formattedId.substring(0, maxDigits);
    }

}
