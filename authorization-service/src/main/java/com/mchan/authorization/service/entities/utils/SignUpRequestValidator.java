package com.mchan.authorization.service.entities.utils;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.service.exceptions.SignUpException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates tha provided SignUpRequest has the minimum valid criteria in order to pass
 * as a correct SignUp Request.
 */
@Component
public class SignUpRequestValidator {

    private final StringUtils stringUtils;
    private final EmailValidator emailValidator;

    /**
     * Constructor.
     *
     * @param stringUtils .
     */
    @Autowired
    public SignUpRequestValidator(StringUtils stringUtils, EmailValidator emailValidator) {
        this.stringUtils = stringUtils;
        this.emailValidator = emailValidator;
    }

    /**
     * .
     *
     * @param request .
     *
     */
    public void validateSignUpRequest(SignUpRequest request) {
        requireNonNullField(request.getFirstName(), "First name is required");
        requireNonNullField(request.getLastName(), "Last name is required");
        requireNonNullField(request.getEmailAddress(), "Email address is required");
        requireNonNullField(request.getPassword(), "A password is required");

        // Now, let's validate that everything is working as expected.
        validateEmailAddress(request.getEmailAddress());

        // Now let's validate that the password is a safe and secure one.
        validatePassword(request.getPassword());
    }

    private void validatePassword(String password) {
        if (!stringUtils.hasValidPassword(password)) {
            throw new SignUpException("The provided password does not match the minimum requirements");
        }
    }

    private void validateEmailAddress(String emailAddress) {
        if (!emailValidator.isValid(emailAddress)) {
            throw new SignUpException(String.format("The email: %s is not valid", emailAddress));
        }
    }

    private void requireNonNullField(String fieldValue, String errorMessage) {
        stringUtils.requireNonEmpty(fieldValue, SignUpException.class, errorMessage);
    }

}
