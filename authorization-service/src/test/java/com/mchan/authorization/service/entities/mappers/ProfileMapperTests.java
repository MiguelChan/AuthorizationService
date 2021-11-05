package com.mchan.authorization.service.entities.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class ProfileMapperTests {

    private ProfileMapper profileMapper;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        profileMapper = new ProfileMapper();
    }

    @Test
    public void fromEntity_should_returnTheProfile() {
        ProfileEntity profileEntity = EnhancedRandom.random(ProfileEntity.class);

        Profile profile = profileMapper.fromEntity(profileEntity);

        assertThat(profile.getProfileId()).isEqualTo(profileEntity.getProfileId());
        assertThat(profile.getFirstName()).isEqualTo(profileEntity.getFirstName());
        assertThat(profile.getLastName()).isEqualTo(profileEntity.getLastName());
        assertThat(profile.getEmailAddress()).isEqualTo(profileEntity.getEmailAddress());
        assertThat(profile.getPhoneNumber()).isEqualTo(profileEntity.getPhoneNumber());
    }
}
