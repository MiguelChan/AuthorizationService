package com.mchan.authorization.service.authorization.integration.tests;

import static org.testng.AssertJUnit.assertTrue;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.models.Application;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.testng.annotations.Test;

/**
 * .
 */
@Test
public class CreateApplicationTests extends BaseTests {

    private static final String TEST_PROFILE_ID = "prF538D4ADE4A846C295";

    @Test
    public void createApplication_should_createTheApplication() throws Exception {
        Application randomApplication = EnhancedRandom.random(Application.class);
        CreateApplicationRequest request = CreateApplicationRequest.builder()
            .application(randomApplication)
            .profileId(TEST_PROFILE_ID)
            .build();

        CreateApplicationResponse response = serviceClient.createApplication(request);

        assertTrue(response.isSuccess());
    }

}
