package com.mchan.authorization.service.entities.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class RandomIdGeneratorTests {

    private RandomIdGenerator randomIdGenerator;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        randomIdGenerator = new RandomIdGenerator();
    }

    @Test
    public void generateRandomId_should_generateA10DigitId() {
        String testPrefix = "test";
        String id = randomIdGenerator.generateRandomId(testPrefix);
        assertThat(id.length()).isEqualTo(20);
    }

    @Test
    public void generateRandomId_should_generateNumberOfAmountOfNumberDigitId() {
        int length = 20;
        String testPrefix = "test";
        String id = randomIdGenerator.generateRandomId(testPrefix, length);

        assertThat(id.length()).isEqualTo(length);
    }

}
