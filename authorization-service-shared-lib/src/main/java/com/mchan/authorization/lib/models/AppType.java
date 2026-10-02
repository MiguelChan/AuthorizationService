package com.mchan.authorization.lib.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The supported application categories exposed by the API.
 */
public enum AppType {
    SERVICE("Service"),
    WEB_SERVICE("WebService");

    private final String value;

    AppType(String value) {
        this.value = value;
    }

    /**
     * Returns the public API value.
     *
     * @return Application category.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Reads a supported category without accepting enum ordinals or unknown values.
     *
     * @param value Public API value.
     * @return Supported application category.
     */
    @JsonCreator
    public static AppType fromValue(String value) {
        for (AppType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("App type must be Service or WebService");
    }
}
