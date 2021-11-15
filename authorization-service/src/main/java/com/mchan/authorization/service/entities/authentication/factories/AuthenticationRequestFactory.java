package com.mchan.authorization.service.entities.authentication.factories;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.models.ClassicAuthenticationRequest;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Simple Utility that creates a specific
 * {@link AuthenticationRequest}.
 */
@Log4j2
@Component
public class AuthenticationRequestFactory {

    /**
     * Gets the {@link AuthenticationRequest} that matches with the provided {@link LogInRequest}.
     *
     * @param request The request to used as a base for the {@link AuthenticationRequest}.
     *
     * @return A fully parsed-ready-to-use {@link AuthenticationRequest}.
     */
    public AuthenticationRequest getAuthRequest(LogInRequest request) {
        if (request.getAuthType() == AuthenticationType.CLASSIC) {
            return buildClassicRequest(request.getUserName(), request.getPassword());
        }
        String errorMessage = String.format("AuthType: %s is not recognized", request.getAuthType());
        log.error(errorMessage);
        throw new InvalidArgumentException(errorMessage);
    }

    /**
     * Creates a {@link ClassicAuthenticationRequest} request.
     *
     * @param username .
     *
     * @param password .
     *
     * @return .
     */
    private AuthenticationRequest buildClassicRequest(String username, String password) {
        return ClassicAuthenticationRequest.builder()
            .password(password)
            .username(username)
            .build();
    }

}
