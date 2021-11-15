package com.mchan.authorization.service.entities.dao.impl;

import com.mchan.authorization.service.entities.dao.SessionsDao;
import com.mchan.authorization.service.entities.dao.entities.SessionEntity;
import com.mchan.authorization.service.entities.dao.mappers.SessionsMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * .
 */
@Log4j2
@Component
public class MyBatisSessionsDao implements SessionsDao {

    private final SessionsMapper sessionsMapper;

    /**
     * .
     *
     * @param sessionsMapper .
     */
    @Autowired
    public MyBatisSessionsDao(SessionsMapper sessionsMapper) {
        this.sessionsMapper = sessionsMapper;
    }

    @Override
    public void createSession(SessionEntity sessionEntity) {
        log.info("Creating Session: {}", sessionEntity);
        sessionsMapper.createSession(sessionEntity);
    }
}
