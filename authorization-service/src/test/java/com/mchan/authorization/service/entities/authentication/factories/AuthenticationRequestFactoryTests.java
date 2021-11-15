package com.mchan.authorization.service.entities.authentication.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.models.ClassicAuthenticationRequest;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class AuthenticationRequestFactoryTests {

    private AuthenticationRequestFactory requestFactory;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        requestFactory = new AuthenticationRequestFactory();
    }

    @Test
    public void getAuthRequest_should_returnTheCorrectAuthRequest() {
        LogInRequest logInRequest = LogInRequest.builder()
            .authType(AuthenticationType.CLASSIC)
            .build();

        AuthenticationRequest authRequest = requestFactory.getAuthRequest(logInRequest);

        assertThat(authRequest).isInstanceOfAny(ClassicAuthenticationRequest.class);
    }

    @Test
    public void getAuthRequest_should_throwInvalidArgumentException_when_providedAuthTypeIsNotSupported() {
        LogInRequest logInRequest = LogInRequest.builder()
            .authType(AuthenticationType.DEFAULT)
            .build();


        assertThatThrownBy(() -> requestFactory.getAuthRequest(logInRequest)).isInstanceOfAny(InvalidArgumentException.class);
    }

}
