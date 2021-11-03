package com.mchan.authorization.service.entities.integration.utils;

/**
 * Called whenever a bad request exists.
 */
public class BadRequestException extends RuntimeException {

    private final BadRequestResponse badRequestResponse;

    public BadRequestException(BadRequestResponse response) {
        super();
        this.badRequestResponse = response;
    }

    public BadRequestException(String message, BadRequestResponse response) {
        super(message);
        this.badRequestResponse = response;
    }

    public BadRequestException(String message,
                               Throwable cause,
                               BadRequestResponse response) {
        super(message, cause);
        this.badRequestResponse = response;
    }
}
