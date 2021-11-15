package com.mchan.authorization.service.entities.authentication.factories;

import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Defines a Factory that can properly instantiate.
 * {@link com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy}.
 */
@Log4j2
@Component
public class AuthenticationStrategyFactory {

    private final Map<AuthenticationType, AuthenticationStrategy> authTypeToStrategy;

    /**
     * Default constructor.
     *
     * @param authTypeToStrategy .
     */
    @Autowired
    public AuthenticationStrategyFactory(
        @Qualifier("authTypeToStrategyMap") Map<AuthenticationType, AuthenticationStrategy> authTypeToStrategy) {
        this.authTypeToStrategy = authTypeToStrategy;
    }

    /**
     * Gets the correct {@link AuthenticationStrategy} based off the {@link AuthenticationType}.
     *
     * @param authType .
     *
     * @return .
     */
    public AuthenticationStrategy getAuthStrategy(AuthenticationType authType) {
        if (!authTypeToStrategy.containsKey(authType)) {
            String errorMessage = String.format("AuthType '%s' is not supported", authType);
            log.error(errorMessage);
            throw new InvalidArgumentException(errorMessage);
        }

        return authTypeToStrategy.get(authType);
    }

}
