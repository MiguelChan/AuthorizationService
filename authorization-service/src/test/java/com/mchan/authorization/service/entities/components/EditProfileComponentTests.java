package com.mchan.authorization.service.entities.components;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class EditProfileComponentTests {

    @Mock
    private ProfileDao profileDao;

    private EditProfileComponent component;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        component = new EditProfileComponent(profileDao);
    }

    @Test
    public void editProfile_should_editTheProfile() {
        String profileId = "AProfileID";
        EditProfileRequest request = EnhancedRandom.random(EditProfileRequest.class);
        ProfileEntity rawProfile = EnhancedRandom.random(ProfileEntity.class);

        when(profileDao.getProfile(profileId)).thenReturn(rawProfile);
        when(profileDao.editProfile(any(ProfileEntity.class))).thenReturn(true);

        boolean profileIsEdited = component.editProfile(profileId, request);

        assertThat(profileIsEdited).isTrue();
    }

    @Test
    public void editProfile_should_onlyUpdateProvidedFields() {
        String profileId = "AProfileID";
        String expectedFirstName = "AFirstName";
        EditProfileRequest request = EditProfileRequest.builder()
            .firstName(expectedFirstName)
            .build();
        ProfileEntity rawProfile = EnhancedRandom.random(ProfileEntity.class);

        ProfileEntity editedProfile = rawProfile.toBuilder()
            .firstName(expectedFirstName)
            .build();

        when(profileDao.getProfile(profileId)).thenReturn(rawProfile);
        when(profileDao.editProfile(editedProfile)).thenReturn(true);

        boolean profileIsEdited = component.editProfile(profileId, request);

        assertThat(profileIsEdited).isTrue();
    }

}
