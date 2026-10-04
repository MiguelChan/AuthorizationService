package com.mchan.authorization.lib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An immutable HTTP method/path/action identity with editable display metadata.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationEndpoint {
    private int endpointId;
    private int applicationId;
    private String httpMethod;
    private String path;
    private String action;
    private String description;
    private boolean active;
}
