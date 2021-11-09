package com.mchan.authorization.service.entities.components;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * The component used for editing a {@link com.mchan.authorization.lib.models.Profile}.
 */
@Log4j2
@Component
public class EditProfileComponent {

    private final ProfileDao profileDao;

    /**
     * .
     *
     * @param profileDao .
     */
    public EditProfileComponent(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    /**
     * .
     *
     * @param profileId .
     *
     * @param request .
     *
     * @return .
     */
    public boolean editProfile(String profileId, EditProfileRequest request) {
        log.info("Attempting to edit profile with Id: {}", profileId);

        // 1.- We check if the Profile Exists
        ProfileEntity rawProfile = profileDao.getProfile(profileId);

        // 2.- We will only edit some attributes and we will only edit them if they're provided.
        // Otherwise, we will keep the original value.
        ProfileEntity dirtyProfile = rawProfile.toBuilder()
            .firstName(firstNonNull(request.getFirstName(), rawProfile.getFirstName()))
            .lastName(firstNonNull(request.getLastName(), rawProfile.getLastName()))
            .phoneNumber(firstNonNull(request.getPhoneNumber(), rawProfile.getPhoneNumber()))
            .build();

        // 3.- We insert.
        return profileDao.editProfile(dirtyProfile);
    }

    private String firstNonNull(String left, String right) {
        if (left != null) {
            return left;
        }
        return right;
    }

}
