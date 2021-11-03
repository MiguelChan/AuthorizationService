package com.mchan.authorization.service.entities.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class SecurePasswordUtilsTests {

    private static final String CUSTOM_PEPPER = "ThisIsAPepper";
    private static final int NUMBER_OF_ITERS = 11;

    private SecurePasswordUtils securePasswordUtils;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        securePasswordUtils = new SecurePasswordUtils(CUSTOM_PEPPER, NUMBER_OF_ITERS);
    }

    @Test
    public void createSecurePassword_should_neverCreateTheSamePasswordTwice() throws Exception {
        final String password = "ThisIsAPassword";
        Set<String> hashedPasswords = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            String securePassword = securePasswordUtils.createSecurePassword(password);
            assertThat(hashedPasswords.contains(securePassword)).isFalse();
            hashedPasswords.add(securePassword);
        }
    }

    @Test
    public void isValidPassword_should_returnValidPassword() throws Exception {
        final String password = "ThisIsAPassword";

        // 1.- We create the password.
        String hashedPassword = securePasswordUtils.createSecurePassword(password);

        // 2.- We validate the password
        boolean isValidPassword = securePasswordUtils.isValidPassword(password, hashedPassword);

        // 3.- We validate
        assertThat(isValidPassword).isTrue();
    }

}
