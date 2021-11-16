package com.mchan.authorization.lib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines an Application. An Application is used in our System to identify a whole set of objects
 * that will be used for authentication and authorization.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Application {
    private int applicationId;
    private String appName;
    private String appIcon;
    private String appHomePage;
    private String shortDescription;
    private String redirectUrl;
}
