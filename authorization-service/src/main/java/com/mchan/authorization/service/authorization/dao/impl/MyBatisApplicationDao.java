package com.mchan.authorization.service.authorization.dao.impl;

import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * .
 */
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
    public void createApplication(ApplicationEntity applicationEntity) {
        applicationsMapper.createApplication(applicationEntity);
    }

}
