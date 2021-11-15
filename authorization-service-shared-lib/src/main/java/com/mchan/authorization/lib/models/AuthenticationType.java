package com.mchan.authorization.lib.models;

/**
 * Defines the Authentication Type.
 */
public enum AuthenticationType {

    /**
     * Classic way of Authenticating by using Username/Password.
     */
    CLASSIC("Classic"),

    /**
     * Unsupported type.
     */
    DEFAULT("Default");

    private final String authType;

    public String getAuthType() {
        return authType;
    }

    AuthenticationType(String authType) {
        this.authType = authType;
    }
}
