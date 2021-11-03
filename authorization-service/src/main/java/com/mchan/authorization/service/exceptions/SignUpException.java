package com.mchan.authorization.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception meant to be thrown when a SignUp Exception occurs.
 */
public class SignUpException extends RuntimeException {

    /**
     * .
     */
    public SignUpException() {
    }

    /**
     * .
     *
     * @param message .
     *
     */
    public SignUpException(String message) {
        super(message);
    }

    /**
     * .
     *
     * @param message .
     *
     * @param cause .
     */
    public SignUpException(String message, Throwable cause) {
        super(message, cause);
    }
}
