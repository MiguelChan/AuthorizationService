package com.mchan.authorization.service.entities.dao.impl;

import static org.mockito.Mockito.verify;

import com.mchan.authorization.service.entities.dao.entities.SessionEntity;
import com.mchan.authorization.service.entities.dao.mappers.SessionsMapper;
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
public class MyBatisSessionsDaoTests {

    @Mock
    private SessionsMapper sessionsMapper;

    private MyBatisSessionsDao sessionsDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        sessionsDao = new MyBatisSessionsDao(sessionsMapper);
    }

    @Test
    public void createSession_should_createTheSession() {
        SessionEntity sessionEntity = EnhancedRandom.random(SessionEntity.class);

        sessionsDao.createSession(sessionEntity);

        verify(sessionsMapper).createSession(sessionEntity);
    }

}
