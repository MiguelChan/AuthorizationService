package com.mchan.authorization.service.authorization.dao.entities;

import java.util.Date;
import lombok.Data;
import lombok.ToString;

/**
 * Opaque token metadata; only the digest of the random token is persisted.
 */
@Data
public class OauthTokenEntity {
    @ToString.Exclude
    private String tokenHash;
    private int sourceId;
    private int targetId;
    private long sourceCredentialVersion;
    private long targetCredentialVersion;
    private String issuer;
    private Date issuedAt;
    private Date expiresAt;
}
