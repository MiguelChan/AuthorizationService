package com.mchan.authorization.service.authorization.dao.impl;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationsMapper;
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
public class MyBatisApplicationDaoTests {

    @Mock
    private ApplicationsMapper applicationsMapper;

    private MyBatisApplicationDao applicationDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        applicationDao = new MyBatisApplicationDao(applicationsMapper);
    }

    @Test
    public void createApplication_should_createTheApp() {
        ApplicationEntity application = EnhancedRandom.random(ApplicationEntity.class);

        int applicationId = applicationDao.createApplication(application);

        verify(applicationsMapper).createApplication(application);
        assertThat(applicationId).isEqualTo(application.getApplicationId());
    }

    @Test
    public void deactivateApplication_should_deactivateTheApp() {
        int expectedAppId = 12355;

        applicationDao.deactivateApplication(expectedAppId);

        verify(applicationsMapper).deactivateApplication(expectedAppId);
    }

    @Test
    public void getApplication_should_returnTheApplication() {
        int expectedAppId = 1234;
        ApplicationEntity expectedApp = EnhancedRandom.random(ApplicationEntity.class);

        when(applicationsMapper.getApplication(expectedAppId)).thenReturn(expectedApp);

        ApplicationEntity foundApp = applicationDao.getApplication(expectedAppId);

        assertThat(foundApp).isEqualTo(expectedApp);
    }

    @Test
    public void updateApplication_should_updateTheApplication() {
        ApplicationEntity appToEdit = EnhancedRandom.random(ApplicationEntity.class);

        when(applicationsMapper.updateApplication(appToEdit)).thenReturn(1);

        int affectedRows = applicationDao.updateApplication(appToEdit);

        assertThat(affectedRows).isEqualTo(1);
    }

}
