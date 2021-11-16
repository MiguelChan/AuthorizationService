package com.mchan.authorization.service.authorization.integration.clients;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;

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

}
