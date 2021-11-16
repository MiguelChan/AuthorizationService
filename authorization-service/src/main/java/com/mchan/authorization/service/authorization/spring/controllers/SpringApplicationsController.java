package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.controllers.ApplicationsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * .
 */
@RestController
@RequestMapping("/")
public class SpringApplicationsController implements ApplicationsController {

    private final CreateApplicationComponent createApplicationComponent;

    @Autowired
    public SpringApplicationsController(CreateApplicationComponent createApplicationComponent) {
        this.createApplicationComponent = createApplicationComponent;
    }

    @PostMapping("/applications")
    @Override
    public CreateApplicationResponse createApplication(@RequestBody CreateApplicationRequest request) {
        createApplicationComponent.createApplication(request);
        return CreateApplicationResponse.builder()
            .success(true)
            .build();
    }
}
