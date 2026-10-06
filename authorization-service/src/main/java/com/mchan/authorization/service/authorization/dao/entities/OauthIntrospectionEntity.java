package com.mchan.authorization.service.authorization.dao.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * One permission row from a single live, recipient-bound authorization snapshot.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OauthIntrospectionEntity extends OauthTokenEntity {
    private String sourceClientId;
    private int endpointId;
    private String action;
    private String httpMethod;
    private String path;
}
