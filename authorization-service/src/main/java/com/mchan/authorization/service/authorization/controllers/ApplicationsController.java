package com.mchan.authorization.service.authorization.controllers;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;

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

}
