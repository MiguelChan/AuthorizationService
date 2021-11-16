package com.mchan.authorization.service.authorization.spring.controllers;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
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
public class SpringApplicationsControllerTests {

    @Mock
    private CreateApplicationComponent createApplicationComponent;

    private SpringApplicationsController appsController;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        appsController = new SpringApplicationsController(createApplicationComponent);
    }

    @Test
    public void createApplication_should_createTheApplication() {
        CreateApplicationRequest expectedRequest = EnhancedRandom.random(CreateApplicationRequest.class);

        CreateApplicationResponse response = appsController.createApplication(expectedRequest);

        assertThat(response.isSuccess()).isTrue();
        verify(createApplicationComponent).createApplication(expectedRequest);
    }

}
