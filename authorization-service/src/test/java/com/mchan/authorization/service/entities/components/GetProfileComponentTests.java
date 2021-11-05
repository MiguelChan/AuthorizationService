package com.mchan.authorization.service.entities.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.mappers.ProfileMapper;
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
public class GetProfileComponentTests {

    @Mock
    private ProfileDao profileDao;
    @Mock
    private ProfileMapper profileMapper;

    private GetProfileComponent component;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        component = new GetProfileComponent(profileDao, profileMapper);
    }

    @Test
    public void getProfile_should_returnTheProfile() {
        String profileId = "SomeProfileId";
        Profile expectedProfile = EnhancedRandom.random(Profile.class);
        ProfileEntity profile = EnhancedRandom.random(ProfileEntity.class);
        when(profileDao.getProfile(profileId)).thenReturn(profile);
        when(profileMapper.fromEntity(profile)).thenReturn(expectedProfile);

        Profile foundProfile = component.getProfile(profileId);

        assertThat(foundProfile).isEqualTo(expectedProfile);
    }

}
