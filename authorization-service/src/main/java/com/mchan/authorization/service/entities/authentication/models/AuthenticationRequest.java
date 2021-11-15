package com.mchan.authorization.service.entities.authentication.models;

import com.mchan.authorization.lib.models.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a type of Authenticatin Request.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class AuthenticationRequest {

    protected AuthenticationType authenticationType;

}
