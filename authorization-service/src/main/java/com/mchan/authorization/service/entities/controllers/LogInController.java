package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.dtos.LogInResponse;

/**
 * The interface for Log Into the System.
 */
public interface LogInController {

    /**
     * The request used for logging into the System.
     *
     * @param request .
     *
     * @return .
     */
    LogInResponse logIn(LogInRequest request);

}
