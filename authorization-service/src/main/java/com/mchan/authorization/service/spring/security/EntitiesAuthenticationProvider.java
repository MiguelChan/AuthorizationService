package com.mchan.authorization.service.spring.security;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.LogInComponent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Provides a {@link AuthenticationProvider} that will connect to the underlying.
 * Entities System in order to Log-In.
 */
@Log4j2
@Component
public class EntitiesAuthenticationProvider implements AuthenticationProvider {

    private final LogInComponent logInComponent;

    /**
     * .
     *
     * @param logInComponent .
     */
    @Autowired
    public EntitiesAuthenticationProvider(LogInComponent logInComponent) {
        this.logInComponent = logInComponent;
        log.info("Initializing the Component");
    }

    /**
     * Authenticates the provided {@link Authentication} object into the EntitiesSystem.
     *
     * @param authentication The current credentials.
     *
     * @return The fully authenticated User.
     *
     * @throws AuthenticationException .
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();

        Profile foundProfile;

        LogInRequest logInRequest = LogInRequest.builder()
            .userName(username)
            .password(password)
            .authType(AuthenticationType.CLASSIC)
            .build();
        try {
            foundProfile = logInComponent.logIn(logInRequest);
        } catch (Exception e) {
            log.error("There was an error while trying to Authenticated", e);
            throw new AuthenticationServiceException(e.getMessage(), e);
        }

        return EntitiesAuthenticationToken.builder()
            .principal(username)
            .credentials(password)
            .profile(foundProfile)
            .build();
    }

    /**
     * .
     *
     * @param authentication .
     *
     * @return .
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return EntitiesAuthenticationToken.class.isAssignableFrom(authentication)
            || UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
