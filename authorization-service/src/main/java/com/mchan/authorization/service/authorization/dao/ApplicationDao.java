package com.mchan.authorization.service.authorization.dao;

import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;

/**
 * The Application Dao.
 */
public interface ApplicationDao {

    /**
     * Creates an Application.
     *
     * @param applicationEntity .
     */
    void createApplication(ApplicationEntity applicationEntity);

}
