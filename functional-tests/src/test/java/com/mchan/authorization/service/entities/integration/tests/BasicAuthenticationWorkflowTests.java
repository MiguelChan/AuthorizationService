package com.mchan.authorization.service.entities.integration.tests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
import com.mchan.authorization.lib.dtos.GetProfileRequest;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.dtos.LogInResponse;
import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.lib.models.Profile;
import io.github.benas.randombeans.api.EnhancedRandom;
import java.util.UUID;
import javax.naming.AuthenticationException;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * Basic AuthN Workflow Tets.
 */
@Test(groups = "basicAuthNWorkflow")
public class BasicAuthenticationWorkflowTests extends BaseTests {

    private static final String KEY_PROFILE_ID = "ProfileId";
    private static final String KEY_RAW_PROFILE = "RawProfile";
    private static final String KEY_USERNAME = "UserNameKey";

    private static final String PASSWORD = "ThisPassword.Be123!";

    @Test
    public void signUp_should_signTheUserUp(ITestContext testContext) throws Exception {
        SignUpRequest request = EnhancedRandom.random(SignUpRequest.class,
            "password",
            "email",
            "phoneNumber"
        );

        String randomEmail = UUID.randomUUID().toString() + "@some.com";
        String randomNumber = "1234567890";

        request.setEmailAddress(randomEmail);
        request.setPassword(PASSWORD);
        request.setPhoneNumber(randomNumber);

        SignUpResponse response = this.serviceClient.signUp(request);

        assertNotNull(response.getProfileId());
        assertFalse(response.getProfileId().isEmpty());

        testContext.setAttribute(KEY_PROFILE_ID, response.getProfileId());
        testContext.setAttribute(KEY_USERNAME, randomEmail);
    }

    @Test(dependsOnMethods = "signUp_should_signTheUserUp")
    public void getProfile_should_returnTheProvidedProfile(ITestContext testContext) throws Exception {
        String profileId = (String) testContext.getAttribute(KEY_PROFILE_ID);
        Profile profile = getProfile(profileId);
        testContext.setAttribute(KEY_RAW_PROFILE, profile);
    }

    @Test(dependsOnMethods = "getProfile_should_returnTheProvidedProfile")
    public void editProfile_should_editTheProfile(ITestContext testContext) throws Exception {
        String expectedNewFirstName = "Carlos";
        String expectedNewLastName = "Santana";
        Profile nonEditedProfile = (Profile) testContext.getAttribute(KEY_RAW_PROFILE);

        EditProfileRequest request = EditProfileRequest.builder()
            .firstName(expectedNewFirstName)
            .lastName(expectedNewLastName)
            .build();

        EditProfileResponse response = this.serviceClient.editProfile(nonEditedProfile.getProfileId(), request);

        assertTrue(response.isSuccess());

        Profile updatedProfile = getProfile(nonEditedProfile.getProfileId());
        assertEquals(updatedProfile.getFirstName(), expectedNewFirstName);
        assertEquals(updatedProfile.getLastName(), expectedNewLastName);
    }

    @Test(dependsOnMethods = "signUp_should_signTheUserUp")
    public void logIn_should_logTheUser(ITestContext testContext) throws Exception {
        String email = (String) testContext.getAttribute(KEY_USERNAME);

        LogInRequest logInRequest = LogInRequest.builder()
            .authType(AuthenticationType.CLASSIC)
            .userName(email)
            .password(PASSWORD)
            .build();

        LogInResponse logInResponse = this.serviceClient.logIn(logInRequest);

        assertTrue(logInResponse.isLoggedIn());
    }

    @Test(dependsOnMethods = "logIn_should_logTheUser", expectedExceptions = AuthenticationException.class)
    public void logIn_should_throwNotAuthorizedException_when_passwordIsWrong(ITestContext testContext)
        throws Exception {
        String email = (String) testContext.getAttribute(KEY_USERNAME);

        LogInRequest request = LogInRequest.builder()
            .password("SomePassword")
            .userName(email)
            .authType(AuthenticationType.CLASSIC)
            .build();

        this.serviceClient.logIn(request);
    }

    private Profile getProfile(String profileId) throws Exception {
        GetProfileRequest request = GetProfileRequest.builder()
            .profileId(profileId)
            .build();

        GetProfileResponse response = serviceClient.getProfile(request);

        assertNotNull(response.getProfile());
        return response.getProfile();
    }

}
