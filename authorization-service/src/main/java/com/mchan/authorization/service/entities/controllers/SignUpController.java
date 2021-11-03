package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;

/**
 * Controller that handles the "Registration" (Sign-Up) of the Users.
 */
public interface SignUpController {

    /**
     * Method that registers a "User" into our System.
     *
     * @param request .
     *
     * @return .
     */
    SignUpResponse signUp(SignUpRequest request);

}
