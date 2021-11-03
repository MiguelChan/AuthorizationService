package com.mchan.authorization.service.entities.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.mchan.authorization.service.exceptions.SignUpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class StringUtilsTests {

    private StringUtils stringUtils;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        stringUtils = new StringUtils();
    }

    @Test
    public void requireNonEmpty_should_throwNullPointException_when_providedValueIsEmptyOrNull() {
        assertThatThrownBy(() -> stringUtils.requireNonEmpty(null)).isInstanceOfAny(NullPointerException.class);
        assertThatThrownBy(() -> stringUtils.requireNonEmpty("")).isInstanceOfAny(NullPointerException.class);
    }

    @Test
    public void requireNonEmpty_should_doNothing_when_providedStringIsNotNullNorEmpty() {
        stringUtils.requireNonEmpty("AString");
        assertThat(true).isTrue();
    }

    @Test
    public void requireNonEmpty_should_throwProvidedException_when_anExceptionIsProvided() {
        assertThatThrownBy(() -> stringUtils.requireNonEmpty(null, SignUpException.class, "Expected a value"))
            .isInstanceOfAny(SignUpException.class)
            .hasMessage("Expected a value");

        assertThatThrownBy(() -> stringUtils.requireNonEmpty("", SignUpException.class, "Expected a value"))
            .isInstanceOfAny(SignUpException.class)
            .hasMessage("Expected a value");
    }

    @Test
    public void requireNonEmpty_should_doNothing_when_providedExceptionDoesNotMeetConstructorCriteria() {
        class CustomException extends RuntimeException {
        }

        stringUtils.requireNonEmpty(null, CustomException.class, "Expected a value");
    }

    @Test
    public void requireNonEmpty_should_doNothing_when_exceptionIsProvidedAndValueIsNotNullNorEmpty() {
        stringUtils.requireNonEmpty("This is a String", SignUpException.class, "Expected a value");
        assertThat(true).isTrue();
    }

    @Test
    public void hasValidPassword_should_returnTrue_when_validPasswordIsProvided() {
        // At least one digit. One lowercase char. One uppercase char. Special chars. min 8 chars
        String validPassword = "ThisIsAPassword#Man1";

        boolean hasValidPassword = stringUtils.hasValidPassword(validPassword);

        assertThat(hasValidPassword).isTrue();
    }

    @Test
    public void hasValidPassword_should_returnFalse_when_validInvalidPasswordIsProvided() {
        String invalidPassword = "thisisnotavalidpass";

        boolean hasValidPassword = stringUtils.hasValidPassword(invalidPassword);

        assertThat(hasValidPassword).isFalse();
    }

}
