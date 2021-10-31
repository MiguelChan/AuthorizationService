package com.mchan.authorization.service.entities.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.dao.mappers.HealthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class MyBatisHealthDaoTests {

    @Mock
    private HealthMapper healthMapper;

    private MyBatisHealthDao healthDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        healthDao = new MyBatisHealthDao(healthMapper);
    }

    @Test
    public void isHealthy_should_returnTrue() {
        when(healthMapper.isHealthy()).thenReturn(true);

        assertThat(healthDao.isHealthy()).isTrue();
    }

    @Test
    public void isHealthy_should_returnFalse_when_anErrorOccurrs() {
        when(healthMapper.isHealthy()).thenThrow(RuntimeException.class);

        assertThat(healthDao.isHealthy()).isFalse();
    }

}
