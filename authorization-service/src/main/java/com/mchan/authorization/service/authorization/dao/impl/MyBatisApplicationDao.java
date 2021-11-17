package com.mchan.authorization.service.authorization.dao.impl;

import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationsMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * .
 */
@Log4j2
@Component
public class MyBatisApplicationDao implements ApplicationDao {

    private final ApplicationsMapper applicationsMapper;

    /**
     * .
     *
     * @param applicationsMapper .
     */
    @Autowired
    public MyBatisApplicationDao(ApplicationsMapper applicationsMapper) {
        this.applicationsMapper = applicationsMapper;
    }

    @Override
    public int createApplication(ApplicationEntity applicationEntity) {
        log.info("Attempting to Create Application: {}", applicationEntity);
        applicationsMapper.createApplication(applicationEntity);
        return applicationEntity.getApplicationId();
    }

    @Override
    public void deactivateApplication(int applicationId) {
        log.info("Attempting to Deactivate Application With Id: {}", applicationId);
        applicationsMapper.deactivateApplication(applicationId);
    }

    @Override
    public ApplicationEntity getApplication(int applicationId) {
        return applicationsMapper.getApplication(applicationId);
    }

    @Override
    public int updateApplication(ApplicationEntity application) {
        return applicationsMapper.updateApplication(application);
    }
}
