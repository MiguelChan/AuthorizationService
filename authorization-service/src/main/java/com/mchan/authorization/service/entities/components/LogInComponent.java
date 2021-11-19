package com.mchan.authorization.service.entities.components;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.authentication.factories.AuthenticationRequestFactory;
import com.mchan.authorization.service.entities.authentication.factories.AuthenticationStrategyFactory;
import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component that handles the "Log" in from a User.
 */
@Component
public class LogInComponent {

    private final AuthenticationStrategyFactory authStrategyFactory;
    private final AuthenticationRequestFactory authRequestBuilder;

    /**
     * Default constructor.
     *
     * @param authStrategyFactory .
     */
    @Autowired
    public LogInComponent(AuthenticationStrategyFactory authStrategyFactory,
                          AuthenticationRequestFactory authRequestBuilder) {
        this.authStrategyFactory = authStrategyFactory;
        this.authRequestBuilder = authRequestBuilder;
    }

    /**
     * Logs a User into our Service.w
     *
     * @param request .
     *
     * @return .
     */
    public Profile logIn(LogInRequest request) {
        AuthenticationType authType = request.getAuthType();

        AuthenticationRequest authRequest = authRequestBuilder.getAuthRequest(request);
        AuthenticationStrategy authStrategy = authStrategyFactory.getAuthStrategy(authType);

        return authStrategy.authenticateUser(authRequest);
    }

}
