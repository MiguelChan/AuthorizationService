package com.mchan.authorization.service.entities.components;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.authentication.factories.AuthenticationRequestFactory;
import com.mchan.authorization.service.entities.authentication.factories.AuthenticationStrategyFactory;
import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class LogInComponentTests {

    @Mock
    private AuthenticationRequestFactory authRequestFactory;
    @Mock
    private AuthenticationStrategyFactory authStrategyFactory;

    private LogInComponent component;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        component = new LogInComponent(authStrategyFactory, authRequestFactory);
    }

    @Test
    public void logIn_should_logTheUserIn() {
        LogInRequest logInRequest = EnhancedRandom.random(LogInRequest.class);
        Profile expectedProfile = EnhancedRandom.random(Profile.class);
        AuthenticationRequest expectedAuthRequest = new AuthenticationRequest() {};
        AuthenticationStrategy authenticationStrategy = mock(AuthenticationStrategy.class);

        when(authRequestFactory.getAuthRequest(logInRequest)).thenReturn(expectedAuthRequest);
        when(authStrategyFactory.getAuthStrategy(logInRequest.getAuthType())).thenReturn(authenticationStrategy);
        when(authenticationStrategy.authenticateUser(expectedAuthRequest)).thenReturn(expectedProfile);

        Profile profile = component.logIn(logInRequest);

        assertThat(profile).isEqualTo(expectedProfile);
    }

}
