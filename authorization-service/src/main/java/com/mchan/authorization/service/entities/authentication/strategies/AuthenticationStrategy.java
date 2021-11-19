package com.mchan.authorization.service.entities.authentication.strategies;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;

/**
 * Defines a Strategy for Authenticating (Logging-in) a User into our System.
 */
public interface AuthenticationStrategy {

    /**
     * Method that authenticates the User given the AuthStrategy.
     *
     * @param authRequest  The request to be used. Take into consideration that the "base" request
     *                     {@link AuthenticationRequest} just contains a simple attribute.
     *                     Implementations of the {@link AuthenticationStrategy} should also create
     *                     a new type of {@link AuthenticationRequest} that will be the one consumed
     *                     by the Strategy.
     *
     * @return The associated Profile with the User.
     */
    Profile authenticateUser(AuthenticationRequest authRequest);

}
