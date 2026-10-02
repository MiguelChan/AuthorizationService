package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Restricts existing application mutations to the profile that owns them.
 */
@Component
public class ApplicationOwnershipComponent {

    private final ApplicationDao applicationDao;

    /**
     * Creates the ownership check using the application's stored profile.
     *
     * @param applicationDao Application persistence.
     */
    public ApplicationOwnershipComponent(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    /**
     * Requires that the authenticated profile owns the requested application.
     *
     * @param applicationId Requested application.
     * @param profileId Authenticated profile.
     */
    public void requireOwner(int applicationId, String profileId) {
        ApplicationEntity application = applicationDao.getApplication(applicationId);
        if (application == null) {
            throw new EntityNotFoundException("Application does not exist");
        }
        if (!profileId.equals(application.getProfileId())) {
            throw new AccessDeniedException("Application belongs to another profile");
        }
    }
}
