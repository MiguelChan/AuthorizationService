package com.mchan.authorization.service.entities.spring.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.service.entities.components.SignUpComponent;
import com.mchan.authorization.service.exceptions.SignUpException;
import java.util.Optional;
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
public class SpringSignUpControllerTests {

    @Mock
    private SignUpComponent signUpComponent;

    private SpringSignUpController signUpController;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        signUpController = new SpringSignUpController(signUpComponent);
    }

    @Test
    public void signUp_should_returnProfileId() throws Exception {
        String expectedProfileId = "AProfileId";
        SignUpRequest signUpRequest = SignUpRequest.builder().build();

        when(signUpComponent.signUp(signUpRequest)).thenReturn(Optional.of(expectedProfileId));

        SignUpResponse response = signUpController.signUp(signUpRequest);

        assertThat(response.getProfileId()).isEqualTo(expectedProfileId);
    }

    @Test
    public void signUp_should_throwException_when_aSignUpExceptionOccurs() throws Exception {
        SignUpRequest signUpRequest = SignUpRequest.builder().build();

        when(signUpComponent.signUp(signUpRequest)).thenThrow(SignUpException.class);

        assertThatThrownBy(() -> signUpController.signUp(signUpRequest))
            .isInstanceOfAny(ResponseStatusException.class);
    }

    @Test
    public void signUp_should_throwException_when_anExceptionOccurs() throws Exception {
        SignUpRequest signUpRequest = SignUpRequest.builder().build();

        when(signUpComponent.signUp(signUpRequest)).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> signUpController.signUp(signUpRequest))
            .isInstanceOfAny(ResponseStatusException.class);
    }

}
