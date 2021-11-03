package com.mchan.authorization.service.entities.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * A simple String Utility class.
 */
@Component
public class StringUtils {

    private static final String PASSWORD_REGEX =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

    /**
     * Validates that the provided password is valid.
     *
     * @param password .
     *
     * @return .
     */
    public boolean hasValidPassword(String password) {
        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        return pattern.matcher(password).matches();
    }

    /**
     * Validates that a given String is not Null nor empty.
     *
     * @param stringToValidate .
     */
    public void requireNonEmpty(String stringToValidate) {
        if (stringToValidate == null || stringToValidate.isEmpty()) {
            throw new NullPointerException("Expected String not to be null");
        }
    }

    /**
     * Validates that a given String is not null nor Empty. Also, throws the provided exceptionType with the provided
     * errorMessage.
     *
     * @param stringToValidate The string to validate.
     *
     * @param exceptionType  The type of exception to throw. Needs to be an instance of {@link RuntimeException} and
     *                       also implement the Exception(String) constructor.
     *
     * @param errorMessage The error message to add to the exceptionType.
     */
    public void requireNonEmpty(String stringToValidate, Class<? extends RuntimeException> exceptionType, String errorMessage) {
        if (stringToValidate == null || stringToValidate.isEmpty()) {
            try {
                throw exceptionType.getConstructor(String.class).newInstance(errorMessage);
            } catch (InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

}
