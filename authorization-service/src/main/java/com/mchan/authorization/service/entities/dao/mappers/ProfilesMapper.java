package com.mchan.authorization.service.entities.dao.mappers;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * .
 */
@Mapper
public interface ProfilesMapper {

    /**
     * .
     *
     * @param profileEntity .
     */
    void createProfile(ProfileEntity profileEntity);

    /**
     * .
     *
     * @param profileId .
     *
     * @return .
     */
    ProfileEntity getProfile(String profileId);

    /**
     * .
     *
     * @param profileEntity .
     */
    void editProfile(ProfileEntity profileEntity);

}
