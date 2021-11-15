package com.mchan.authorization.service.entities.authentication.models;

import com.mchan.authorization.lib.models.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Defines a "Classic" Auth Request.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClassicAuthenticationRequest extends AuthenticationRequest {

    private String username;
    private String password;

    /**
     * .
     *
     * @param username .
     *
     * @param password .
     */
    @Builder(toBuilder = true)
    public ClassicAuthenticationRequest(String username, String password) {
        super(AuthenticationType.CLASSIC);
        this.username = username;
        this.password = password;
    }
}
