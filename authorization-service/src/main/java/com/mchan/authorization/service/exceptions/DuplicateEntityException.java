package com.mchan.authorization.service.exceptions;

/**
 * USed when MyBatis throws a duplicate element.
 */
public class DuplicateEntityException extends RuntimeException {

    /**
     * .
     *
     * @param message .
     * @param cause .
     */
    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }

}
