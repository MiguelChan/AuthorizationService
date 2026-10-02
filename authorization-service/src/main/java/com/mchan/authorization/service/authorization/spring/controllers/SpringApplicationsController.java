package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;
import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.components.ApplicationOwnershipComponent;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.authorization.controllers.ApplicationsController;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * .
 */
@RestController
@RequestMapping("/api")
public class SpringApplicationsController implements ApplicationsController {

    private final CreateApplicationComponent createApplicationComponent;
    private final DeleteApplicationComponent deactivateApplicationComponent;
    private final UpdateApplicationComponent updateApplicationComponent;
    private final AuthenticationFacade authenticationFacade;
    private final ApplicationOwnershipComponent applicationOwnershipComponent;

    /**
     * .
     *
     * @param createApplicationComponent .
     *
     * @param deleteApplicationComponent .
     *
     * @param updateApplicationComponent .
     *
     * @param authenticationFacade Authenticated profile access.
     *
     * @param applicationOwnershipComponent Stored ownership checks.
     */
    @Autowired
    public SpringApplicationsController(CreateApplicationComponent createApplicationComponent,
                                        DeleteApplicationComponent deleteApplicationComponent,
                                        UpdateApplicationComponent updateApplicationComponent,
                                        AuthenticationFacade authenticationFacade,
                                        ApplicationOwnershipComponent applicationOwnershipComponent) {
        this.createApplicationComponent = createApplicationComponent;
        this.deactivateApplicationComponent = deleteApplicationComponent;
        this.updateApplicationComponent = updateApplicationComponent;
        this.authenticationFacade = authenticationFacade;
        this.applicationOwnershipComponent = applicationOwnershipComponent;
    }

    @PostMapping("/applications")
    @Override
    public CreateApplicationResponse createApplication(@RequestBody CreateApplicationRequest request) {
        String profileId = authenticationFacade.getAuthenticationToken().getProfile().getProfileId();
        if (request.getProfileId() != null && !profileId.equals(request.getProfileId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create applications for another profile");
        }
        CreateApplicationRequest ownedRequest = CreateApplicationRequest.builder()
            .application(request.getApplication())
            .profileId(profileId)
            .build();
        try {
            int applicationId = createApplicationComponent.createApplication(ownedRequest);
            return CreateApplicationResponse.builder()
                .applicationId(applicationId)
                .build();
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile does not exist", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create application", e);
        }
    }

    @DeleteMapping("/applications/{applicationId}")
    @Override
    public DeactivateApplicationResponse deactivateApplication(@PathVariable("applicationId") int applicationId) {
        requireOwner(applicationId);
        try {
            deactivateApplicationComponent.deleteApplication(applicationId);
            return DeactivateApplicationResponse.builder()
                .success(true)
                .build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to deactivate application", e);
        }
    }

    @PutMapping("/applications/{applicationId}")
    @Override
    public UpdateApplicationResponse updateApplication(@PathVariable("applicationId") int applicationId,
                                                       @RequestBody UpdateApplicationRequest request) {
        requireOwner(applicationId);
        try {
            if (request.getApplication() == null) {
                throw new InvalidArgumentException("Application is required");
            }
            Application appToUpdate = request.getApplication().toBuilder()
                .applicationId(applicationId)
                .build();

            boolean isUpdated = updateApplicationComponent.updateApplication(appToUpdate);

            return UpdateApplicationResponse.builder()
                .success(isUpdated)
                .build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update application", e);
        }
    }

    private void requireOwner(int applicationId) {
        String profileId = authenticationFacade.getAuthenticationToken().getProfile().getProfileId();
        try {
            applicationOwnershipComponent.requireOwner(applicationId, profileId);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to access application", e);
        }
    }
}
