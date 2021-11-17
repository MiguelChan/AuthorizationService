package com.mchan.authorization.service.authorization.integration.clients;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationRequest;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;

/**
 * .
 */
public interface AuthorizationServiceClient {

    /**
     * .
     *
     * @param request .
     *
     * @return .
     */
    CreateApplicationResponse createApplication(CreateApplicationRequest request) throws Exception;

    /**
     * .
     *
     * @param request .
     *
     * @return .
     */
    UpdateApplicationResponse updateApplication(UpdateApplicationRequest request) throws Exception;

    /**
     * .
     *
     * @param request .
     *
     * @return .
     *
     * @throws Exception .
     */
    DeactivateApplicationResponse deactivateApplication(DeactivateApplicationRequest request) throws Exception;

}
