package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Covers origin syntax independently of network access or a hard-coded TLD list.
 */
public class ApplicationInputValidatorTests {
    private final ApplicationInputValidator validator = new ApplicationInputValidator();

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com", "https://example.com/", "http://localhost:8094/",
        "https://example.dev", "http://127.0.0.1:8080", "https://[::1]:443/", "HTTP://EXAMPLE.COM", "https://example.com:65535"})
    public void validOrigins_should_beAccepted(String value) {
        validator.validateRedirectUrl(value);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "/callback", "example.com", "//example.com", "file:///tmp/app", "ftp://example.com",
        "javascript:alert(1)", "https://", "https://example..com", "https://example.com/callback", "https://example.com/%2F",
        "https://example.com?next=app", "https://example.com/?", "https://example.com#fragment", "https://example.com/#",
        "https://user:secret@example.com", "https://example.com:0", "https://example.com:65536", "https://example.com:",
        "https://999.999.999.999", "https://example.com /", "https://[broken]"})
    public void invalidOrigins_should_beRejected(String value) {
        assertThatThrownBy(() -> validator.validateRedirectUrl(value)).isInstanceOf(InvalidArgumentException.class);
    }
}
