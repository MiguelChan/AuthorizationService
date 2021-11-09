package com.mchan.authorization.service.entities.spring.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.EditProfileComponent;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
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

    @Mock
    private GetProfileComponent getProfileComponent;
    @Mock
    private EditProfileComponent editProfileComponent;

    private SpringProfileController profileController;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        profileController = new SpringProfileController(getProfileComponent, editProfileComponent);
    }

    @Test
    public void getProfile_should_returnTheProfile() {
        String profileId = "AProfileId";
        Profile expectedProfile = EnhancedRandom.random(Profile.class);
        when(getProfileComponent.getProfile(profileId)).thenReturn(expectedProfile);

        GetProfileResponse response = profileController.getProfile(profileId);

        assertThat(response.getProfile()).isNotNull();
        assertThat(response.getProfile()).isEqualTo(expectedProfile);
    }

    @Test
    public void getProfile_should_return500_when_theComponentFails() {
        String profileId = "AProfileId";
        String expectedErrorMessage = "AnErrorMessage";
        RuntimeException thrownException = new RuntimeException(expectedErrorMessage);
        when(getProfileComponent.getProfile(profileId)).thenThrow(thrownException);

        assertThatThrownBy(() -> profileController.getProfile(profileId))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500")
            .hasMessageContaining(expectedErrorMessage);
    }

    @Test
    public void getProfile_should_return404_when_profileDoesNotExist() {
        String profileId = "AProfileId";
        String expectedErrorMessage = "AnErrorMessage";
        RuntimeException thrownException = new EntityNotFoundException(expectedErrorMessage);
        when(getProfileComponent.getProfile(profileId)).thenThrow(thrownException);

        assertThatThrownBy(() -> profileController.getProfile(profileId))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("404")
            .hasMessageContaining(expectedErrorMessage);
    }

    @Test
    public void editProfile_should_editTheProfile() {
        String profileId = "AProfileId";
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);

        when(editProfileComponent.editProfile(profileId, request)).thenReturn(true);

        EditProfileResponse response = profileController.editProfile(profileId, request);

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void editProfile_should_return404_when_profileDoesNotExist() {
        String profileId = "AProfileId";
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);

        when(editProfileComponent.editProfile(profileId, request)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> profileController.editProfile(profileId, request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("404");
    }

    @Test
    public void editProfile_should_throw500_when_internalErrorOccurs() {
        String profileId = "AProfileId";
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);

        when(editProfileComponent.editProfile(profileId, request)).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> profileController.editProfile(profileId, request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500");
    }

}
