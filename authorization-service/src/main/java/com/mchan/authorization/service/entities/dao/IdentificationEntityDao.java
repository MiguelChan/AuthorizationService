package com.mchan.authorization.service.entities.dao;

import com.mchan.authorization.service.entities.dao.entities.IdentificationEntity;

/**
 * Defines the DAO for accessing and storing Entities aka. IdentificationEntity.
 */
public interface IdentificationEntityDao {

    /**
     * Creates a new IdentificationEntiy in the Database.
     *
     * @param identificationEntity .
     *
     * @return The newly Identity/Entity ID.
     */
    String createIdentificationEntity(IdentificationEntity identificationEntity);

}
