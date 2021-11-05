package com.mchan.authorization.service.exceptions;

/**
 * Thrown by the DAO's when something can't be found.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * .
     */
    public EntityNotFoundException() {
        super();
    }

    /**
     * .
     *
     * @param message .
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * .
     *
     * @param message .
     *
     * @param cause .
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
