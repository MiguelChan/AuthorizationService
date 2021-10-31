package com.mchan.authorization.service.entities.spring.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.service.entities.dao.HealthDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class SpringHealthControllerTests {

    @Mock
    private HealthDao healthDao;

    private SpringHealthController springHealthController;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        springHealthController = new SpringHealthController(healthDao);
    }

    @Test
    public void ping_should_alwaysReturnTrue() {
        PingRequest pingRequest = PingRequest.builder().build();

        PingResponse pingResponse = springHealthController.ping(pingRequest);

        assertThat(pingResponse.isHealthy()).isTrue();
    }

    @Test
    public void deepPing_should_returnTrue_when_daoIsFine() {
        when(healthDao.isHealthy()).thenReturn(true);
        PingRequest pingRequest = PingRequest.builder().build();

        PingResponse pingResponse = springHealthController.deepPing(pingRequest);

        assertThat(pingResponse.isHealthy()).isTrue();
    }

    @Test
    public void deepPing_should_returnFalse_when_daoIsNotWorking() {
        when(healthDao.isHealthy()).thenReturn(false);
        PingRequest pingRequest = PingRequest.builder().build();

        PingResponse pingResponse = springHealthController.deepPing(pingRequest);

        assertThat(pingResponse.isHealthy()).isFalse();
    }

}
