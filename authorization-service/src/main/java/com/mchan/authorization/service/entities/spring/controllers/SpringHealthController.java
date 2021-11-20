package com.mchan.authorization.service.entities.spring.controllers;

import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.service.entities.controllers.HealthController;
import com.mchan.authorization.service.entities.dao.HealthDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * .
 */
@RestController
@RequestMapping("/")
public class SpringHealthController implements HealthController {

    private final HealthDao healthDao;

    @Autowired
    public SpringHealthController(HealthDao healthDao) {
        this.healthDao = healthDao;
    }

    @GetMapping(value = "/ping", produces = "application/json")
    @Override
    public PingResponse ping(PingRequest request) {
        return PingResponse.builder()
            .healthy(true)
            .build();
    }

    @GetMapping(value = "/deep_ping", produces = "application/json")
    @Override
    public PingResponse deepPing(PingRequest request) {
        boolean isHealthy = healthDao.isHealthy();
        return PingResponse.builder()
            .healthy(isHealthy)
            .build();
    }
}
