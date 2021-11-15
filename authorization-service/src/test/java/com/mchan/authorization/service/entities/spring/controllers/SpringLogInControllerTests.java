package com.mchan.authorization.service.entities.spring.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.dtos.LogInResponse;
import com.mchan.authorization.service.entities.components.LogInComponent;
import com.mchan.authorization.service.exceptions.NotAuthorizedException;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class SpringLogInControllerTests {

    @Mock
    private LogInComponent logInComponent;

    private SpringLogInController controller;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        controller = new SpringLogInController(logInComponent);
    }

    @Test
    public void logIn_should_logTheUserIn() {
        LogInRequest logInRequest = EnhancedRandom.random(LogInRequest.class);
        when(logInComponent.logIn(logInRequest)).thenReturn(true);

        LogInResponse response = controller.logIn(logInRequest);

        assertThat(response.isLoggedIn()).isTrue();
    }

    @Test
    public void logIn_should_return500_when_theComponentFails() {
        LogInRequest request = EnhancedRandom.random(LogInRequest.class);
        when(logInComponent.logIn(request)).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> controller.logIn(request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500");
    }

    @Test
    public void logIn_should_return401_when_notAuthorized() {
        LogInRequest request = EnhancedRandom.random(LogInRequest.class);
        when(logInComponent.logIn(request)).thenThrow(NotAuthorizedException.class);

        assertThatThrownBy(() -> controller.logIn(request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("401");
    }

}
