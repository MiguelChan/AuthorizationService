package com.mchan.authorization.service.entities.utils;

import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * A Simple Utility for Dates.
 */
@Component
public class DateProvider {

    /**
     * Provides the current Date.
     *
     * @return .
     */
    public Instant now() {
        return Instant.now();
    }

}
