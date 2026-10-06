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
    private final LoginAttemptGuard attempts;

    /**
     * .
     *
     * @param logInComponent .
     */
    @Autowired
    public EntitiesAuthenticationProvider(LogInComponent logInComponent, LoginAttemptGuard attempts) {
        this.logInComponent = logInComponent;
        this.attempts = attempts;
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
        String username = java.util.Objects.toString(authentication.getPrincipal(), null);
        String password = java.util.Objects.toString(authentication.getCredentials(), null);

        attempts.enter(username, password);
        Profile foundProfile;

        LogInRequest logInRequest = LogInRequest.builder()
            .userName(username)
            .password(password)
            .authType(AuthenticationType.CLASSIC)
            .build();
        try {
            foundProfile = logInComponent.logIn(logInRequest);
            attempts.succeeded(username);
        } catch (com.mchan.authorization.service.exceptions.NotAuthorizedException e) {
            attempts.failed(username);
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("There was an error while trying to Authenticate.", e);
            throw new AuthenticationServiceException("Authentication temporarily unavailable", e);
        } finally {
            attempts.release();
        }

        return EntitiesAuthenticationToken.builder()
            .principal(username)
            .credentials(null)
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
