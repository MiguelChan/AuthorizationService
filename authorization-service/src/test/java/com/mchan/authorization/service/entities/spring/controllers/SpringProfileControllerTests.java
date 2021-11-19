package com.mchan.authorization.service.entities.spring.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.EditProfileComponent;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import io.github.benas.randombeans.api.EnhancedRandom;
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
public class SpringProfileControllerTests {

    private static final String TEST_PROFILE_ID = "12345";

    @Mock
    private GetProfileComponent getProfileComponent;
    @Mock
    private EditProfileComponent editProfileComponent;
    @Mock
    private AuthenticationFacade authenticationFacade;

    private SpringProfileController profileController;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        profileController = new SpringProfileController(getProfileComponent, editProfileComponent, authenticationFacade);

        Profile mockProfile = Profile.builder()
            .profileId(TEST_PROFILE_ID)
            .build();

        EntitiesAuthenticationToken authenticationToken = mock(EntitiesAuthenticationToken.class);
        when(authenticationToken.getProfile()).thenReturn(mockProfile);
        when(authenticationFacade.getAuthenticationToken()).thenReturn(authenticationToken);
    }

    @Test
    public void getProfile_should_returnTheProfile() {
        Profile expectedProfile = EnhancedRandom.random(Profile.class);
        when(getProfileComponent.getProfile(TEST_PROFILE_ID)).thenReturn(expectedProfile);

        GetProfileResponse response = profileController.getProfile();

        assertThat(response.getProfile()).isNotNull();
        assertThat(response.getProfile()).isEqualTo(expectedProfile);
    }

    @Test
    public void getProfile_should_return500_when_theComponentFails() {
        String expectedErrorMessage = "AnErrorMessage";
        RuntimeException thrownException = new RuntimeException(expectedErrorMessage);
        when(getProfileComponent.getProfile(TEST_PROFILE_ID)).thenThrow(thrownException);

        assertThatThrownBy(() -> profileController.getProfile())
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500")
            .hasMessageContaining(expectedErrorMessage);
    }

    @Test
    public void getProfile_should_return404_when_profileDoesNotExist() {
        String expectedErrorMessage = "AnErrorMessage";
        RuntimeException thrownException = new EntityNotFoundException(expectedErrorMessage);
        when(getProfileComponent.getProfile(TEST_PROFILE_ID)).thenThrow(thrownException);

        assertThatThrownBy(() -> profileController.getProfile())
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("404")
            .hasMessageContaining(expectedErrorMessage);
    }

    @Test
    public void editProfile_should_editTheProfile() {
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);

        when(editProfileComponent.editProfile(TEST_PROFILE_ID, request)).thenReturn(true);

        EditProfileResponse response = profileController.editProfile(request);

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void editProfile_should_return404_when_profileDoesNotExist() {
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);

        when(editProfileComponent.editProfile(TEST_PROFILE_ID, request)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> profileController.editProfile(request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("404");
    }

    @Test
    public void editProfile_should_throw500_when_internalErrorOccurs() {
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);

        when(editProfileComponent.editProfile(TEST_PROFILE_ID, request)).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> profileController.editProfile(request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500");
    }

}
