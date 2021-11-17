package com.mchan.authorization.service.utils;

import org.springframework.stereotype.Component;

/**
 * Simple utilities for Objects.
 */
@Component
public final class ObjectUtils {

    /**
     * Returns the first object that is not null.
     *
     * @param left .
     *
     * @param right .
     *
     * @param <T> .
     *
     * @return .
     */
    public <T> T firstNonNull(T left, T right) {
        if (left == null) {
            return right;
        }
        return left;
    }

}
