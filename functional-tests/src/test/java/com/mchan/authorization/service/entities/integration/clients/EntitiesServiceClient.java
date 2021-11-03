package com.mchan.authorization.service.entities.integration.clients;

import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;

/**
 * Defines the EntitiesService Client.
 */
public interface EntitiesServiceClient {

    /**
     * Performs a simple Ping Operation.
     *
     * @param request .
     *
     * @return .
     *
     * @throws Exception .
     */
    PingResponse ping(PingRequest request) throws Exception;

    /**
     * Performs a simple DeepPing Operation.
     *
     * @param request .
     *
     * @return .
     *
     * @throws Exception .
     */
    PingResponse deepPing(PingRequest request) throws Exception;

    /**
     * Signs a User up in the System.
     *
     * @param request .
     *
     * @return .
     *
     * @throws Exception .
     */
    SignUpResponse signUp(SignUpRequest request) throws Exception;

}
