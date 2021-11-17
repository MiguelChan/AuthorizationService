package com.mchan.authorization.service.authorization.controllers;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;

/**
 * Simple interface that defines the behavior of the Application Controller.
 */
public interface ApplicationsController {

    /**
     * Creates an {@link com.mchan.authorization.lib.models.Application} with the provided parameters.
     *
     * @param request .
     *
     * @return .
     */
    CreateApplicationResponse createApplication(CreateApplicationRequest request);

    /**
     * Deactivates the {@link com.mchan.authorization.lib.models.Application} by Id.
     *
     * @param applicationId .
     *
     * @return .
     */
    DeactivateApplicationResponse deactivateApplication(int applicationId);

    /**
     * Updates the provided {@link com.mchan.authorization.lib.models.Application}.
     *
     * @param applicationId .
     *
     * @param request .
     *
     * @return .
     */
    UpdateApplicationResponse updateApplication(int applicationId, UpdateApplicationRequest request);
}
