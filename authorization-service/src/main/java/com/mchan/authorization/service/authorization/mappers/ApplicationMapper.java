package com.mchan.authorization.service.authorization.mappers;

import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import org.springframework.stereotype.Component;

/**
 * Maps {@link ApplicationEntity} into {@link Application} and viceversa.
 */
@Component
public class ApplicationMapper {

    /**
     * .
     *
     * @param application .
     *
     * @return .
     */
    public ApplicationEntity fromApplication(Application application) {
        return ApplicationEntity.builder()
            .applicationId(application.getApplicationId())
            .appName(application.getAppName())
            .appIcon(application.getAppIcon())
            .appHomePage(application.getAppHomePage())
            .shortDescription(application.getShortDescription())
            .redirectUrl(application.getRedirectUrl())
            .build();
    }

}
