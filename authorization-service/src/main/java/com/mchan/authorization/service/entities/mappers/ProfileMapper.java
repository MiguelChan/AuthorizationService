package com.mchan.authorization.service.entities.mappers;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import org.springframework.stereotype.Component;

/**
 * A Mapper that converts from {@link com.mchan.authorization.service.entities.dao.entities.ProfileEntity}
 * into a {@link com.mchan.authorization.lib.models.Profile} and viceversa.
 */
@Component
public class ProfileMapper {

    /**
     * .
     *
     * @param profileEntity .
     *
     * @return .
     */
    public Profile fromEntity(ProfileEntity profileEntity) {
        return Profile.builder()
            .profileId(profileEntity.getProfileId())
            .firstName(profileEntity.getFirstName())
            .lastName(profileEntity.getLastName())
            .phoneNumber(profileEntity.getPhoneNumber())
            .emailAddress(profileEntity.getEmailAddress())
            .build();
    }

}
