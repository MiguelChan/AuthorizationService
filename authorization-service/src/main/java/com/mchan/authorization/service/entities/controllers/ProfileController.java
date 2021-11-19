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
     * The controller will just return the current active profile from the Authenticated User.
     * If no user is authenticated thehn this method won't get reached.
     *
     * @return .
     */
    GetProfileResponse getProfile();

    /**
     * Edits the current active profile.
     * The authenticated user's profile is the one that is going to be edited.
     *
     * @param request .
     *
     * @return .
     */
    EditProfileResponse editProfile(EditProfileRequest request);

}
