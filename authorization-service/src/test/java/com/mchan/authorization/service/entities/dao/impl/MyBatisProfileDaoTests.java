package com.mchan.authorization.service.entities.dao.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.dao.mappers.ProfilesMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
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
}
