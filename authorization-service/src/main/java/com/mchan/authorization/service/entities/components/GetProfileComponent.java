package com.mchan.authorization.service.entities.components;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.mappers.ProfileMapper;
import org.springframework.stereotype.Component;

/**
 * The Component that is in charge of retrieving the Profile.
 */
@Component
public class GetProfileComponent {

    private final ProfileDao profileDao;
    private final ProfileMapper profileMapper;

    /**
     * .
     *
     * @param profileDao .
     */
    public GetProfileComponent(ProfileDao profileDao, ProfileMapper profileMapper) {
        this.profileDao = profileDao;
        this.profileMapper = profileMapper;
    }

    /**
     * Gets the Profile.
     *
     * @param profileId .
     *
     * @return .
     */
    public Profile getProfile(String profileId) {
        ProfileEntity foundProfile = profileDao.getProfile(profileId);
        return profileMapper.fromEntity(foundProfile);
    }

}
