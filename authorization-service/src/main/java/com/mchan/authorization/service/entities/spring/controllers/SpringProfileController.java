package com.mchan.authorization.service.entities.spring.controllers;

import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.EditProfileComponent;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.entities.controllers.ProfileController;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The rest controller for the {@link com.mchan.authorization.service.entities.controllers.ProfileController}.
 */
@Api(
    authorizations = @Authorization("basic")
)
@Log4j2
@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class SpringProfileController implements ProfileController {

    private final GetProfileComponent getProfileComponent;
    private final EditProfileComponent editProfileComponent;

    private final AuthenticationFacade authenticationFacade;

    /**
     * .
     *
     * @param getProfileComponent .
     */
    @Autowired
    public SpringProfileController(GetProfileComponent getProfileComponent,
                                   EditProfileComponent editProfileComponent,
                                   AuthenticationFacade authenticationFacade) {
        this.getProfileComponent = getProfileComponent;
        this.editProfileComponent = editProfileComponent;
        this.authenticationFacade = authenticationFacade;
    }

    /**
     * .
     *
     * @return .
     */
    @GetMapping(value = "/profile")
    public GetProfileResponse getProfile() {
        log.info("Attempting to retrieve profile.");
        EntitiesAuthenticationToken authToken = authenticationFacade.getAuthenticationToken();
        Profile currentProfile = authToken.getProfile();
        String profileId = currentProfile.getProfileId();
        try {
            Profile profile = getProfileComponent.getProfile(profileId);
            return GetProfileResponse.builder()
                .profile(profile)
                .build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * .
     *
     * @param request .
     *
     * @return .
     */
    @PutMapping(value = "/profile", consumes = "application/json")
    @Override
    public EditProfileResponse editProfile(@RequestBody EditProfileRequest request) {
        Profile currentProfile = authenticationFacade.getAuthenticationToken().getProfile();
        String profileId = currentProfile.getProfileId();
        try {
            boolean success = editProfileComponent.editProfile(profileId, request);
            return EditProfileResponse.builder()
                .success(success)
                .build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}
