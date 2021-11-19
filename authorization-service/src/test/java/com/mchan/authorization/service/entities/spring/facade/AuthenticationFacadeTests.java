package com.mchan.authorization.service.entities.spring.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * .
 */
public class AuthenticationFacadeTests {

    private AuthenticationFacade authenticationFacade;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        authenticationFacade = new AuthenticationFacade();
    }

    @Test
    public void getAuthenticationToken_should_returnTheEntitiesAuthenticationToken() {
        EntitiesAuthenticationToken expectedToken = mock(EntitiesAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(expectedToken);

        EntitiesAuthenticationToken authToken = authenticationFacade.getAuthenticationToken();

        assertThat(authToken).isEqualTo(expectedToken);
    }

    @Test
    public void getAuthenticationToken_should_throwRuntimeException_when_theTokenIsNotEntitiesAuthenticationToken() {
        Authentication mockToken = mock(UsernamePasswordAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(mockToken);

        assertThatThrownBy(() -> authenticationFacade.getAuthenticationToken()).isInstanceOfAny(RuntimeException.class);
    }

}
