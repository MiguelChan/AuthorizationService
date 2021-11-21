package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;
import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.authorization.controllers.ApplicationsController;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * .
 */
@RestController
@RequestMapping("/api")
public class SpringApplicationsController implements ApplicationsController {

    private final CreateApplicationComponent createApplicationComponent;
    private final DeleteApplicationComponent deactivateApplicationComponent;
    private final UpdateApplicationComponent updateApplicationComponent;

    /**
     * .
     *
     * @param createApplicationComponent .
     *
     * @param deleteApplicationComponent .
     *
     * @param updateApplicationComponent .
     */
    @Autowired
    public SpringApplicationsController(CreateApplicationComponent createApplicationComponent,
                                        DeleteApplicationComponent deleteApplicationComponent,
                                        UpdateApplicationComponent updateApplicationComponent) {
        this.createApplicationComponent = createApplicationComponent;
        this.deactivateApplicationComponent = deleteApplicationComponent;
        this.updateApplicationComponent = updateApplicationComponent;
    }

    @PostMapping("/applications")
    @Override
    public CreateApplicationResponse createApplication(@RequestBody CreateApplicationRequest request) {
        int applicationId = createApplicationComponent.createApplication(request);
        return CreateApplicationResponse.builder()
            .applicationId(applicationId)
            .build();
    }

    @DeleteMapping("/applications/{applicationId}")
    @Override
    public DeactivateApplicationResponse deactivateApplication(@PathVariable("applicationId") int applicationId) {
        try {
            deactivateApplicationComponent.deleteApplication(applicationId);
            return DeactivateApplicationResponse.builder()
                .success(true)
                .build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PutMapping("/applications/{applicationId}")
    @Override
    public UpdateApplicationResponse updateApplication(@PathVariable("applicationId") int applicationId,
                                                       @RequestBody UpdateApplicationRequest request) {
        try {
            Application appToUpdate = request.getApplication().toBuilder()
                .applicationId(applicationId)
                .build();

            boolean isUpdated = updateApplicationComponent.updateApplication(appToUpdate);

            return UpdateApplicationResponse.builder()
                .success(isUpdated)
                .build();
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
