package com.mchan.authorization.service.entities.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.service.exceptions.SignUpException;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.apache.commons.validator.routines.EmailValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class SignUpRequestValidatorTests {

    @Mock
    private StringUtils stringUtils;
    @Mock
    private EmailValidator emailValidator;

    private SignUpRequestValidator requestValidator;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        requestValidator = new SignUpRequestValidator(stringUtils, emailValidator);
    }

    @Test
    public void validateSignUpRequest_should_pass_when_allProvidedParametersAreSet() {
        SignUpRequest request = EnhancedRandom.random(SignUpRequest.class);
        when(stringUtils.hasValidPassword(anyString())).thenReturn(true);
        when(emailValidator.isValid(anyString())).thenReturn(true);

        requestValidator.validateSignUpRequest(request);

        verify(stringUtils).requireNonEmpty(eq(request.getFirstName()), eq(SignUpException.class), anyString());
        verify(stringUtils).requireNonEmpty(eq(request.getLastName()), eq(SignUpException.class), anyString());
        verify(stringUtils).requireNonEmpty(eq(request.getPassword()), eq(SignUpException.class), anyString());
        verify(stringUtils).requireNonEmpty(eq(request.getEmailAddress()), eq(SignUpException.class), anyString());
    }

    @Test
    public void validateSignUpRequest_should_throwSignUpException_when_providedEmailIsInvalid() {
        SignUpRequest request = EnhancedRandom.random(SignUpRequest.class);
        when(emailValidator.isValid(anyString())).thenReturn(false);

        assertThatThrownBy(() -> requestValidator.validateSignUpRequest(request)).isInstanceOfAny(SignUpException.class);
    }

    @Test
    public void validateSignUpRequest_should_throwSignUpException_when_providedPasswordIsInvalid() {
        SignUpRequest request = EnhancedRandom.random(SignUpRequest.class);
        when(emailValidator.isValid(anyString())).thenReturn(true);
        when(stringUtils.hasValidPassword(anyString())).thenReturn(false);

        assertThatThrownBy(() -> requestValidator.validateSignUpRequest(request)).isInstanceOfAny(SignUpException.class);
    }

}
