package com.mchan.authorization.service.authorization.dao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An Application meant to be stored in the Data Storage.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class ApplicationEntity {
    private int applicationId;
    private String profileId;
    private String appName;
    private String appIcon;
    private String appHomePage;
    private String shortDescription;
    private String redirectUrl;
}
