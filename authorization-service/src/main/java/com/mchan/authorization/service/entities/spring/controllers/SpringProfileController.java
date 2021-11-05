package com.mchan.authorization.service.entities.spring.controllers;

import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.entities.controllers.ProfileController;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The rest controller for the {@link com.mchan.authorization.service.entities.controllers.ProfileController}.
 */
@Log4j2
@RestController("/")
public class SpringProfileController implements ProfileController {

    private final GetProfileComponent getProfileComponent;

    /**
     * .
     *
     * @param getProfileComponent .
     */
    @Autowired
    public SpringProfileController(GetProfileComponent getProfileComponent) {
        this.getProfileComponent = getProfileComponent;
    }

    /**
     * .
     *
     * @param profileId .
     *
     * @return .
     */
    @GetMapping(value = "/auth/profile/{profileId}", produces = "application/json")
    @Override
    public GetProfileResponse getProfile(@PathVariable("profileId") String profileId) {
        try {
            Profile profile = getProfileComponent.getProfile(profileId);
            return GetProfileResponse.builder()
                .profile(profile)
                .build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error(e);
            log.info(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}
