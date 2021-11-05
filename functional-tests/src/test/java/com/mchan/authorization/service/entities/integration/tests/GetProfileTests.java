package com.mchan.authorization.service.entities.integration.tests;


import static org.testng.AssertJUnit.assertNotNull;

import com.mchan.authorization.lib.dtos.GetProfileRequest;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import org.testng.annotations.Test;

/**
 * .
 */
@Test
public class GetProfileTests extends BaseTests {

    private static final String TEST_PROFILE_ID = "pr4E154B2F6A074DED8B";

    @Test
    public void getProfile_should_returnTheProfile() throws Exception {
        GetProfileRequest request = GetProfileRequest.builder()
            .profileId(TEST_PROFILE_ID)
            .build();

        GetProfileResponse response = serviceClient.getProfile(request);

        assertNotNull(response.getProfile());
    }

    @Test(expectedExceptions = ClassNotFoundException.class)
    public void getProfile_should_throw404_when_profileDoesNotExist() throws Exception {
        GetProfileRequest request = GetProfileRequest.builder()
            .profileId("ThisShouldNotExist")
            .build();
        serviceClient.getProfile(request);
    }

}
