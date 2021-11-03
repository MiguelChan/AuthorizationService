package com.mchan.authorization.service.entities.dao;

import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;

/**
 * Defines the Profile DAO.
 */
public interface ProfileDao {

    /**
     * Creates a Profile with the provided parameters.
     * Returns the generated ProfileId.
     * Throws an Exception should an error occur.
     *
     * @param profileEntity .
     *
     * @return The generated Profile Id.
     */
    String createProfile(ProfileEntity profileEntity);

}
