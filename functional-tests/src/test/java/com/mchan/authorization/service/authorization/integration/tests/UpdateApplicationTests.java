package com.mchan.authorization.service.authorization.integration.tests;

import static org.testng.AssertJUnit.assertTrue;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;
import com.mchan.authorization.lib.models.Application;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * .
 */
@Test
public class UpdateApplicationTests extends BaseTests {

    private static final String TEST_PROFILE_ID = "prF538D4ADE4A846C295";

    @Test
    public void updateApplication_should_updateTheApplication(ITestContext testContext) throws Exception {
        int applicationId = createApplication();
        Application randomApp = EnhancedRandom.random(Application.class, "applicationId");

        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
            .applicationId(applicationId)
            .application(randomApp)
            .build();

        UpdateApplicationResponse response = serviceClient.updateApplication(request);

        assertTrue(response.isSuccess());
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
