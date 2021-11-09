package com.mchan.authorization.service.entities.controllers;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
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

    /**
     * Edits the provided profile.
     *
     * @param profileId .
     *
     * @param request .
     *
     * @return .
     */
    EditProfileResponse editProfile(String profileId, EditProfileRequest request);

}
