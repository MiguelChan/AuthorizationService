package com.mchan.authorization.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * .
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidArgumentException extends RuntimeException {

    /**
     * .
     */
    public InvalidArgumentException() {
        super();
    }

    /**
     * .
     *
     * @param message .
     */
    public InvalidArgumentException(String message) {
        super(message);
    }

    /**
     * .
     *
     * @param message .
     *
     * @param cause .
     */
    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
