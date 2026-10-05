package com.mchan.authorization.service.exceptions;

import lombok.Getter;

/**
 * A controlled OAuth protocol error with no submitted credential material.
 */
@Getter
public class OauthException extends RuntimeException {
    private final String error;
    private final int status;

    /**
     * Creates a protocol error and its HTTP status.
     */
    public OauthException(String error, int status) {
        super(error);
        this.error = error;
        this.status = status;
    }
}
