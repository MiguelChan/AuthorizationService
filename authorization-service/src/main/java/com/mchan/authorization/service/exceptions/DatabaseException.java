package com.mchan.authorization.service.exceptions;

/**
 * A Simple Database Exception.
 */
public class DatabaseException extends RuntimeException {

    /**
     * .
     *
     * @param message .
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * .
     *
     * @param message .
     *
     * @param cause .
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
