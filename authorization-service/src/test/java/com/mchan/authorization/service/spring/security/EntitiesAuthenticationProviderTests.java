package com.mchan.authorization.service.spring.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.LogInComponent;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class EntitiesAuthenticationProviderTests {

    @Mock
    private LogInComponent logInComponent;

    private EntitiesAuthenticationProvider authenticationProvider;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        authenticationProvider = new EntitiesAuthenticationProvider(logInComponent);
    }

    @Test
    public void authenticate_should_returnLoggedInUser() {
        String username = "AnUserName";
        String password = "APassword";

        LogInRequest expectedLogInRequest = LogInRequest.builder()
            .authType(AuthenticationType.CLASSIC)
            .userName(username)
            .password(password)
            .build();

        Profile expectedProfile = EnhancedRandom.random(Profile.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(username);
        when(authentication.getCredentials()).thenReturn(password);

        when(logInComponent.logIn(expectedLogInRequest)).thenReturn(expectedProfile);

        Authentication authenticatedUser = authenticationProvider.authenticate(authentication);

        assertThat(authenticatedUser).isInstanceOfAny(EntitiesAuthenticationToken.class);

        EntitiesAuthenticationToken authToken = (EntitiesAuthenticationToken) authenticatedUser;
        assertThat(authToken.getProfile()).isEqualTo(expectedProfile);
        assertThat(authToken.getPrincipal()).isEqualTo(username);
        assertThat(authToken.getCredentials()).isEqualTo(password);
    }

    @Test
    public void authenticate_should_throw_authenticationServiceException_when_theLogInComponentFails() {
        String username = "AnUserName";
        String password = "APassword";

        LogInRequest expectedLogInRequest = LogInRequest.builder()
            .authType(AuthenticationType.CLASSIC)
            .userName(username)
            .password(password)
            .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(username);
        when(authentication.getCredentials()).thenReturn(password);

        when(logInComponent.logIn(expectedLogInRequest)).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> authenticationProvider.authenticate(authentication))
            .isInstanceOfAny(AuthenticationServiceException.class);
    }

    @Test
    public void supports_should_onlySupportUsernamePasswordOrEntitiesAuthToken() {
        assertThat(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
        assertThat(authenticationProvider.supports(EntitiesAuthenticationToken.class)).isTrue();
        assertThat(authenticationProvider.supports(TestingAuthenticationToken.class)).isFalse();
    }

}
