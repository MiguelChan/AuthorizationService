package com.mchan.authorization.service.entities.utils;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class DateProviderTests {

    private DateProvider dateProvider;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        dateProvider = new DateProvider();
    }

    @Test
    public void now_should_returnTwoDifferentDates() throws Exception {
        Instant currentTime = dateProvider.now();

        Thread.sleep(1000);

        Instant newTime = dateProvider.now();

        assertThat(currentTime).isNotEqualTo(newTime);
    }

}
