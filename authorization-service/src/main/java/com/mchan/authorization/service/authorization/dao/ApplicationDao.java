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
     *
     * @return The Application Id.
     */
    int createApplication(ApplicationEntity applicationEntity);

    /**
     * Deactivates an {@link com.mchan.authorization.lib.models.Application}.
     *
     * @param applicationId .
     */
    void deactivateApplication(int applicationId);

    /**
     * Gets an {@link ApplicationEntity} from the Storage.
     *
     * @param applicationId .
     *
     * @return .
     */
    ApplicationEntity getApplication(int applicationId);

    /**
     * Updates the provided {@link ApplicationEntity}.
     *
     * @param application .
     *
     * @return How many rows where affected by this operation.
     */
    int updateApplication(ApplicationEntity application);

}
