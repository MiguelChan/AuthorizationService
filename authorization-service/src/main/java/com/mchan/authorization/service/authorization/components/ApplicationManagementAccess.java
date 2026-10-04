package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Applies stored user ownership to catalog and grant management routes.
 */
@Component
public class ApplicationManagementAccess {
    private final ApplicationOwnershipComponent ownership;
    private final AuthenticationFacade authentication;

    /**
     * Creates user ownership checks, separate from client authentication.
     */
    public ApplicationManagementAccess(ApplicationOwnershipComponent ownership, AuthenticationFacade authentication) {
        this.ownership = ownership;
        this.authentication = authentication;
    }

    /**
     * Requires the current user to own the application.
     */
    public void requireOwner(int applicationId) {
        try {
            ownership.requireOwner(applicationId, authentication.getAuthenticationToken().getProfile().getProfileId());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application does not exist");
        }
    }
}
