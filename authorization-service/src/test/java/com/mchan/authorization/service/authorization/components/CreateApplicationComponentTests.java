package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.mappers.ApplicationMapper;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
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
public class CreateApplicationComponentTests {

    @Mock
    private ProfileDao profileDao;
    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private ApplicationMapper applicationMapper;

    private CreateApplicationComponent component;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        component = new CreateApplicationComponent(profileDao, applicationDao, applicationMapper);
    }

    @Test
    public void createApplication_should_throwInvalidArgumentException_when_theProfileDoesNotExist() {
        CreateApplicationRequest request = EnhancedRandom.random(CreateApplicationRequest.class);
        when(profileDao.getProfile(any())).thenReturn(null);

        assertThatThrownBy(() -> component.createApplication(request)).isInstanceOfAny(InvalidArgumentException.class);
    }

    @Test
    public void createApplication_should_createTheApplication() {
        String profileId = "AProfileId";
        final int expectedAppId = 12345;

        ProfileEntity profileEntity = EnhancedRandom.random(ProfileEntity.class, "profileId");
        profileEntity.setProfileId(profileId);

        CreateApplicationRequest request = EnhancedRandom.random(CreateApplicationRequest.class, "profileId");
        request.setProfileId(profileId);

        ApplicationEntity applicationEntity = EnhancedRandom.random(ApplicationEntity.class);
        when(applicationMapper.fromApplication(request.getApplication())).thenReturn(applicationEntity);

        ApplicationEntity expectedAppToCreate = applicationEntity.toBuilder()
            .profileId(profileId)
            .build();
        when(applicationDao.createApplication(expectedAppToCreate)).thenReturn(expectedAppId);
        when(profileDao.getProfile(profileId)).thenReturn(profileEntity);

        int createdAppId = component.createApplication(request);

        verify(applicationDao).createApplication(expectedAppToCreate);
        assertThat(createdAppId).isEqualTo(expectedAppId);
    }

}
