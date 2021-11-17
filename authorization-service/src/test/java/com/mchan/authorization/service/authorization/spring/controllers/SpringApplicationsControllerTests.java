package com.mchan.authorization.service.authorization.spring.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;
import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class SpringApplicationsControllerTests {

    @Mock
    private CreateApplicationComponent createApplicationComponent;
    @Mock
    private DeleteApplicationComponent deleteApplicationComponent;
    @Mock
    private UpdateApplicationComponent updateApplicationComponent;

    private SpringApplicationsController appsController;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        appsController = new SpringApplicationsController(createApplicationComponent, deleteApplicationComponent, updateApplicationComponent);
    }

    @Test
    public void createApplication_should_createTheApplication() {
        int expectedAppId = 12345;
        CreateApplicationRequest expectedRequest = EnhancedRandom.random(CreateApplicationRequest.class);
        when(createApplicationComponent.createApplication(any())).thenReturn(expectedAppId);

        CreateApplicationResponse response = appsController.createApplication(expectedRequest);

        assertThat(response.getApplicationId()).isEqualTo(expectedAppId);
        verify(createApplicationComponent).createApplication(expectedRequest);
    }

    @Test
    public void deactivateApplication_should_deactivateTheApp() {
        int expectedAppId = 12345;

        DeactivateApplicationResponse response = appsController.deactivateApplication(expectedAppId);

        assertThat(response.isSuccess()).isTrue();
        verify(deleteApplicationComponent).deleteApplication(expectedAppId);
    }

    @Test
    public void deactivateApplication_should_throwNotFound_when_theEntityDoesNotExist() {
        doThrow(EntityNotFoundException.class).when(deleteApplicationComponent).deleteApplication(anyInt());

        assertThatThrownBy(() -> appsController.deactivateApplication(12345))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("404");
    }

    @Test
    public void deactivateApplication_should_throwBadRequest_when_theComponentThrowsInvalidArgumentException() {
        doThrow(InvalidArgumentException.class).when(deleteApplicationComponent).deleteApplication(anyInt());

        assertThatThrownBy(() -> appsController.deactivateApplication(12345))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("400");
    }

    @Test
    public void deactivateApplication_should_throwInternalServerException_when_thereIsRandomError() {
        doThrow(RuntimeException.class).when(deleteApplicationComponent).deleteApplication(anyInt());

        assertThatThrownBy(() -> appsController.deactivateApplication(12345))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500");
    }

    @Test
    public void updateApplication_should_updateTheApplication() {
        int expectedAppId = 1235;
        Application midApp = EnhancedRandom.random(Application.class);
        Application expectedApp = midApp.toBuilder()
            .applicationId(expectedAppId)
            .build();
        UpdateApplicationRequest request = UpdateApplicationRequest.builder()
            .application(midApp)
            .build();

        when(updateApplicationComponent.updateApplication(expectedApp)).thenReturn(true);

        UpdateApplicationResponse response = appsController.updateApplication(expectedAppId, request);

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void updateApplication_should_throwBadRequest_when_invalidArgumentExceptionIsThrown() {
        UpdateApplicationRequest request = EnhancedRandom.random(UpdateApplicationRequest.class);
        when(updateApplicationComponent.updateApplication(any())).thenThrow(InvalidArgumentException.class);

        assertThatThrownBy(() -> appsController.updateApplication(12345, request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("400");
    }

    @Test
    public void updateApplication_should_throwBadRequest_when_randomExceptionIsThrown() {
        UpdateApplicationRequest request = EnhancedRandom.random(UpdateApplicationRequest.class);
        when(updateApplicationComponent.updateApplication(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> appsController.updateApplication(12345, request))
            .isInstanceOfAny(ResponseStatusException.class)
            .hasMessageContaining("500");
    }


}
