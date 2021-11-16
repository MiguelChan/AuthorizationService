package com.mchan.authorization.service.authorization.dao.impl;

import static org.mockito.Mockito.verify;

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

        applicationDao.createApplication(application);

        verify(applicationsMapper).createApplication(application);
    }

}
