package com.mchan.authorization.service.entities.integration.tests;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.testng.annotations.Test;

/**
 * Basic SignUp Tests.
 * These tests just validate that the Profile can be created and also that we can't input
 * any weird data.
 */
public class SignUpTests extends BaseTests {

    @Test
    public void signUp_should_signTheUserUp_when_allTheParametersAreCorrect() throws Exception {
        SignUpRequest request = EnhancedRandom.random(SignUpRequest.class,
            "password",
            "email",
            "phoneNumber"
        );

        String securePassword = "ThisPassword.Be123!";
        String randomEmail = "somesome@some.com";
        String randomNumber = "1234567890";

        request.setEmailAddress(randomEmail);
        request.setPassword(securePassword);
        request.setPhoneNumber(randomNumber);

        SignUpResponse response = this.serviceClient.signUp(request);

        assertNotNull(response.getProfileId());
        assertFalse(response.getProfileId().isEmpty());
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void signUp_should_returnBadRequest_when_theProvidedRequestIsInvalid() throws Exception {
        // An Empty Request
        SignUpRequest request = SignUpRequest.builder()
            .build();

        this.serviceClient.signUp(request);
    }

}
