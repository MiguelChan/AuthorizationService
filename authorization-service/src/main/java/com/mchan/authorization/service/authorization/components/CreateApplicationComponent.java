package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.mappers.ApplicationMapper;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component that handles the Creation of an {@link Application}.
 */
@Log4j2
@Component
public class CreateApplicationComponent {

    private final ProfileDao profileDao;
    private final ApplicationDao applicationDao;
    private final ApplicationMapper applicationMapper;

    /**
     * .
     *
     * @param profileDao .
     *
     * @param applicationDao .
     *
     * @param applicationMapper .
     */
    @Autowired
    public CreateApplicationComponent(ProfileDao profileDao,
                                      ApplicationDao applicationDao,
                                      ApplicationMapper applicationMapper) {
        this.profileDao = profileDao;
        this.applicationDao = applicationDao;
        this.applicationMapper = applicationMapper;
    }

    /**
     * Creates the {@link Application}.
     *
     * @param request .
     */
    public int createApplication(CreateApplicationRequest request) {
        String profileId = request.getProfileId();
        Application application = request.getApplication();

        ProfileEntity profileEntity = getProfile(profileId);
        ApplicationEntity appToStore = applicationMapper.fromApplication(application);
        appToStore = appToStore.toBuilder()
            .profileId(profileEntity.getProfileId())
            .build();

        return applicationDao.createApplication(appToStore);
    }

    private ProfileEntity getProfile(String profileId) {
        ProfileEntity foundProfile = profileDao.getProfile(profileId);
        if (foundProfile == null) {
            String errorMessage = String.format("Profile with Id: '%s' does not exist ", profileId);
            log.error(errorMessage);
            throw new InvalidArgumentException(errorMessage);
        }
        return foundProfile;
    }

}
