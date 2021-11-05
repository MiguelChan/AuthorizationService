package com.mchan.authorization.service.entities.dao.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.dao.mappers.ProfilesMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
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
public class MyBatisProfileDaoTests {

    @Mock
    private ProfilesMapper profilesMapper;
    @Mock
    private RandomIdGenerator randomIdGenerator;

    private MyBatisProfileDao profilesDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        profilesDao = new MyBatisProfileDao(randomIdGenerator, profilesMapper);
    }

    @Test
    public void createProfile_should_createTheProfile() {
        String expectedProfileId = "ProfileId";
        ProfileEntity profileEntity = EnhancedRandom.random(ProfileEntity.class, "profileId");
        ProfileEntity expectedProfileEntity = profileEntity.toBuilder()
            .profileId(expectedProfileId)
            .build();

        when(randomIdGenerator.generateRandomId(any())).thenReturn(expectedProfileId);

        String profileId = profilesDao.createProfile(profileEntity);

        assertThat(profileId).isEqualTo(expectedProfileId);
        verify(profilesMapper).createProfile(eq(expectedProfileEntity));
    }

    @Test
    public void getProfile_should_returnTheProfile() {
        String profileId = "ProfileId";
        ProfileEntity expectedProfile = EnhancedRandom.random(ProfileEntity.class);
        when(profilesMapper.getProfile(profileId)).thenReturn(expectedProfile);

        ProfileEntity foundProfile = profilesDao.getProfile(profileId);

        assertThat(foundProfile).isEqualTo(expectedProfile);
    }

    @Test
    public void getProfile_should_throwEntityNotFoundException_when_profileDoesNotExist() {
        String profileId = "ProfileId";
        when(profilesMapper.getProfile(profileId)).thenReturn(null);

        assertThatThrownBy(() -> profilesDao.getProfile(profileId))
            .isInstanceOfAny(EntityNotFoundException.class)
            .hasMessageContaining("does not exist");
    }
}
