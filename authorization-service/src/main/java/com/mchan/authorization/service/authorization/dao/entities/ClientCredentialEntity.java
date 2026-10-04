package com.mchan.authorization.service.authorization.dao.entities;

import lombok.Data;
import lombok.ToString;

/**
 * Persisted client identity and one-way password hash.
 */
@Data
public class ClientCredentialEntity {
    private int applicationId;
    private String clientId;
    @ToString.Exclude
    private String secretHash;
    private long version;
}
