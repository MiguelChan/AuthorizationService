package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.GetProfileResponse;

/**
 * The controller that will handle the access to {@link com.mchan.authorization.lib.models.Profile}s.
 */
public interface ProfileController {

    /**
     * The request for retrieving a {@link com.mchan.authorization.lib.models.Profile}.
     *
     * @param profileId .
     *
     * @return .
     */
    GetProfileResponse getProfile(String profileId);

}
