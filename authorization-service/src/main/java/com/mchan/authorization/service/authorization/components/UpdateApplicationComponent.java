package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.utils.ObjectUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The component that updates an {@link Application}.
 */
@Log4j2
@Component
public class UpdateApplicationComponent {

    private final ApplicationDao applicationDao;
    private final ObjectUtils objectUtils;

    /**
     * .
     *
     * @param applicationDao .
     *
     * @param objectUtils .
     */
    @Autowired
    public UpdateApplicationComponent(ApplicationDao applicationDao, ObjectUtils objectUtils) {
        this.applicationDao = applicationDao;
        this.objectUtils = objectUtils;
    }

    /**
     * Updates the Application.
     *
     * @param application .
     *
     * @return .
     */
    public boolean updateApplication(Application application) {
        ApplicationEntity dbApp = validateApplicationState(application.getApplicationId());

        ApplicationEntity appToUpdate = dbApp.toBuilder()
            .appName(objectUtils.firstNonNull(application.getAppName(), dbApp.getAppName()))
            .appIcon(objectUtils.firstNonNull(application.getAppIcon(), dbApp.getAppIcon()))
            .appHomePage(objectUtils.firstNonNull(application.getAppHomePage(), dbApp.getAppHomePage()))
            .shortDescription(objectUtils.firstNonNull(application.getShortDescription(), dbApp.getShortDescription()))
            .redirectUrl(objectUtils.firstNonNull(application.getRedirectUrl(), dbApp.getRedirectUrl()))
            .build();

        int updatedRows = applicationDao.updateApplication(appToUpdate);

        return updatedRows > 0;
    }

    private ApplicationEntity validateApplicationState(int applicationId) {
        ApplicationEntity dbApp = applicationDao.getApplication(applicationId);

        if (!dbApp.isActive()) {
            String errorMessage = String.format("Application with Id: '%d' cant be modified", applicationId);
            log.error(errorMessage);
            throw new InvalidArgumentException(errorMessage);
        }

        return dbApp;
    }
}
