package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;

/**
 * Defines the Interface for the HealthController.
 */
public interface HealthController {

    /**
     * Defines a simple ping Operation.
     *
     * @param request .
     *
     * @return .
     */
    PingResponse ping(PingRequest request);

    /**
     * Defines a DeepPing ping that will be used to also validate that Databases
     * and external dependencies are Ok.
     *
     * @param request .
     *
     * @return .
     */
    PingResponse deepPing(PingRequest request);

}
