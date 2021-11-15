package com.mchan.authorization.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the User is not authorized.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotAuthorizedException extends RuntimeException {

    /**
     * .
     */
    public NotAuthorizedException() {
        super();
    }

    /**
     * .
     *
     * @param message .
     */
    public NotAuthorizedException(String message) {
        super(message);
    }

    /**
     * .
     *
     * @param message .
     *
     * @param cause .
     */
    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
