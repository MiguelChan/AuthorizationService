package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * One-time credential delivery; no read endpoint returns the secret.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientCredentials {
    private String clientId;
    @ToString.Exclude
    private String clientSecret;
    private long version;
}
