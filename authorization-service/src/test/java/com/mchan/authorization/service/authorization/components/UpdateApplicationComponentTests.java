package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.utils.ObjectUtils;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class UpdateApplicationComponentTests {

    @Mock
    private ApplicationDao applicationDao;

    /**
     * For the sake of simplicity I'm not mocking this object.
     */
    private ObjectUtils objectUtils = new ObjectUtils();

    private UpdateApplicationComponent component;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        component = new UpdateApplicationComponent(applicationDao, objectUtils, new ApplicationInputValidator());
    }

    @Test
    public void updateApplication_should_updateTheApp() {
        int expectedAppId = 12345;

        Application newApp = EnhancedRandom.random(Application.class, "applicationId");
        newApp.setApplicationId(expectedAppId);
        newApp.setRedirectUrl("https://example.com");

        ApplicationEntity databaseApp = EnhancedRandom.random(ApplicationEntity.class, "isActive");
        databaseApp.setActive(true);

        ApplicationEntity appToCreate = databaseApp.toBuilder()
            .appName(newApp.getAppName())
            .appIcon(newApp.getAppIcon())
            .appHomePage(newApp.getAppHomePage())
            .shortDescription(newApp.getShortDescription())
            .redirectUrl(newApp.getRedirectUrl())
            .appType(newApp.getAppType())
            .build();

        when(applicationDao.getApplication(expectedAppId)).thenReturn(databaseApp);
        when(applicationDao.updateApplication(appToCreate)).thenReturn(1);

        boolean updated = component.updateApplication(newApp);

        assertThat(updated).isTrue();
    }

    @Test
    public void updateApplication_should_returnFalse_when_theUpdateHasFailed() {
        int expectedAppId = 12345;

        Application newApp = EnhancedRandom.random(Application.class, "applicationId");
        newApp.setApplicationId(expectedAppId);
        newApp.setRedirectUrl("https://example.com");

        ApplicationEntity databaseApp = EnhancedRandom.random(ApplicationEntity.class, "isActive");
        databaseApp.setActive(true);

        ApplicationEntity appToCreate = databaseApp.toBuilder()
            .appName(newApp.getAppName())
            .appIcon(newApp.getAppIcon())
            .appHomePage(newApp.getAppHomePage())
            .shortDescription(newApp.getShortDescription())
            .redirectUrl(newApp.getRedirectUrl())
            .appType(newApp.getAppType())
            .build();

        when(applicationDao.getApplication(expectedAppId)).thenReturn(databaseApp);
        when(applicationDao.updateApplication(appToCreate)).thenReturn(0);

        boolean updated = component.updateApplication(newApp);

        assertThat(updated).isFalse();
    }

    @Test
    public void updateApplication_should_throwIllegalArgumentException_when_theApplicationIsNotActive() {
        int expectedAppId = 12345;

        Application newApp = EnhancedRandom.random(Application.class, "applicationId");
        newApp.setApplicationId(expectedAppId);
        newApp.setRedirectUrl("https://example.com");

        ApplicationEntity databaseApp = EnhancedRandom.random(ApplicationEntity.class, "isActive");
        databaseApp.setActive(false);

        when(applicationDao.getApplication(expectedAppId)).thenReturn(databaseApp);

        assertThatThrownBy(() -> component.updateApplication(newApp)).isInstanceOfAny(InvalidArgumentException.class);
    }

    @Test
    public void updateApplication_should_reportMissingApplicationWithoutWriting() {
        Application application = Application.builder().applicationId(7).build();
        assertThatThrownBy(() -> component.updateApplication(application)).isInstanceOf(EntityNotFoundException.class);
        org.mockito.Mockito.verify(applicationDao, org.mockito.Mockito.never()).updateApplication(org.mockito.ArgumentMatchers.any());
    }


    @Test
    public void updateApplication_should_rejectInvalidUrlsBeforeReadingOrWriting() {
        Application app = Application.builder().applicationId(7).redirectUrl("https://example.com?next=app").build();
        assertThatThrownBy(() -> component.updateApplication(app)).isInstanceOf(InvalidArgumentException.class);
        org.mockito.Mockito.verifyNoInteractions(applicationDao);
    }

    @Test
    public void partialUpdate_should_preserveStoredTypeAndLegacyUrl() {
        ApplicationEntity stored = ApplicationEntity.builder().applicationId(7).profileId("owner").isActive(true)
            .appType(com.mchan.authorization.lib.models.AppType.WEB_SERVICE).redirectUrl("https://example.com/legacy").build();
        when(applicationDao.getApplication(7)).thenReturn(stored);
        ApplicationEntity expected = stored.toBuilder().appName("changed").build();
        when(applicationDao.updateApplication(expected)).thenReturn(1);
        assertThat(component.updateApplication(Application.builder().applicationId(7).appName("changed").build())).isTrue();
        org.mockito.Mockito.verify(applicationDao).updateApplication(expected);
    }
}
