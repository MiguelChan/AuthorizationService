package com.mchan.authorization.service.authorization.dao.entities;

import lombok.Data;

/**
 * Live token permissions joined to the immutable receiving endpoint identity.
 */
@Data
public class OauthPermissionEntity {
    private int endpointId;
    private String action;
    private String httpMethod;
    private String path;
}
