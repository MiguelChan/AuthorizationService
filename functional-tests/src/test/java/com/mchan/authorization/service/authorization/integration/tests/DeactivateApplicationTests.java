package com.mchan.authorization.service.authorization.integration.tests;

import static org.testng.AssertJUnit.assertTrue;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationRequest;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.integration.utils.Constants;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * .
 */
@Test
public class DeactivateApplicationTests extends BaseTests {

    private static final String TEST_PROFILE_ID = "prF538D4ADE4A846C295";

    @Test
    public void deactivateApplication_should_deactivateTheApplication(ITestContext context) throws Exception {
        int applicationId = createApplication();

        DeactivateApplicationRequest request = DeactivateApplicationRequest.builder()
            .applicationId(applicationId)
            .build();

        DeactivateApplicationResponse response = serviceClient.deactivateApplication(request);

        assertTrue(response.isSuccess());

        context.setAttribute(Constants.KEY_APPLICATION_ID, applicationId);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void deactivateApplication_should_throwError_when_applicationIsDisabled(ITestContext testContext) throws Exception {
        int applicationId = (Integer) testContext.getAttribute(Constants.KEY_APPLICATION_ID);

        DeactivateApplicationRequest request = DeactivateApplicationRequest.builder()
            .applicationId(applicationId)
            .build();

        serviceClient.deactivateApplication(request);
    }

    private int createApplication() throws Exception {
        Application randomApplication = EnhancedRandom.random(Application.class);
        CreateApplicationRequest request = CreateApplicationRequest.builder()
            .application(randomApplication)
            .profileId(TEST_PROFILE_ID)
            .build();

        CreateApplicationResponse response = serviceClient.createApplication(request);

        assertTrue(response.getApplicationId() > 0);

        return response.getApplicationId();
    }
}
