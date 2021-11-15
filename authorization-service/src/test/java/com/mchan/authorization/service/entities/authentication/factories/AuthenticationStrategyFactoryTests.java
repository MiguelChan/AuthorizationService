package com.mchan.authorization.service.entities.authentication.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class AuthenticationStrategyFactoryTests {

    private Map<AuthenticationType, AuthenticationStrategy> authTypeToAuthStrat;

    private AuthenticationStrategyFactory factory;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        authTypeToAuthStrat = new HashMap<>();
        factory = new AuthenticationStrategyFactory(authTypeToAuthStrat);
    }

    @Test
    public void getAuthStrategy_should_throwAruntimeException_when_theProvidedAuthTypeDoesNotExist() {
        // The Map is initially Empty
        assertThatThrownBy(() -> factory.getAuthStrategy(AuthenticationType.CLASSIC))
            .isInstanceOfAny(InvalidArgumentException.class);
    }

    @Test
    public void getAuthStrategy_should_returnTheRequestedAuthStrategy() {
        AuthenticationStrategy testAuthStrat = mock(AuthenticationStrategy.class);
        authTypeToAuthStrat.put(AuthenticationType.CLASSIC, testAuthStrat);

        AuthenticationStrategy authStrat = factory.getAuthStrategy(AuthenticationType.CLASSIC);

        assertThat(authStrat).isEqualTo(testAuthStrat);
    }

}
