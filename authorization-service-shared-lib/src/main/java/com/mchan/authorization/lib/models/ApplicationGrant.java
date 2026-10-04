package com.mchan.authorization.lib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A source application's permission to call one target endpoint, in one direction.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationGrant {
    private int grantId;
    private int sourceApplicationId;
    private int targetApplicationId;
    private int endpointId;
    private String action;
    private long version;
    private boolean active;
}
