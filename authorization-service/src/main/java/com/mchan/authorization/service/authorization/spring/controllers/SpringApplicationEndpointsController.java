package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.lib.models.ApplicationEndpoint;
import com.mchan.authorization.service.authorization.components.ApplicationEndpointsComponent;
import com.mchan.authorization.service.authorization.components.ApplicationManagementAccess;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Owner-controlled endpoint catalog; these routes do not invoke registered resources.
 */
@RestController
@RequestMapping("/api/applications/{appId}/endpoints")
public class SpringApplicationEndpointsController {
    private final ApplicationEndpointsComponent endpoints;
    private final ApplicationManagementAccess access;

    /**
     * Creates owner-controlled endpoint management.
     */
    public SpringApplicationEndpointsController(ApplicationEndpointsComponent endpoints, ApplicationManagementAccess access) {
        this.endpoints = endpoints;
        this.access = access;
    }

    /**
     * Registers a method, path and action identity.
     */
    @PostMapping
    public ApplicationEndpoint create(@PathVariable int appId, @RequestBody ApplicationEndpoint endpoint) {
        access.requireOwner(appId);
        return endpoints.create(appId, endpoint);
    }

    /**
     * Lists catalog metadata including deactivated entries.
     */
    @GetMapping
    public List<ApplicationEndpoint> list(@PathVariable int appId) {
        access.requireOwner(appId);
        return endpoints.list(appId);
    }

    /**
     * Updates an endpoint description without changing authorization identity.
     */
    @PutMapping("/{endpointId}")
    public ApplicationEndpoint update(@PathVariable int appId, @PathVariable int endpointId, @RequestBody ApplicationEndpoint endpoint) {
        access.requireOwner(appId);
        return endpoints.update(appId, endpointId, endpoint);
    }

    /**
     * Deactivates a catalog entry and therefore blocks future authorization.
     */
    @DeleteMapping("/{endpointId}")
    public ResponseEntity<Void> deactivate(@PathVariable int appId, @PathVariable int endpointId) {
        access.requireOwner(appId);
        endpoints.deactivate(appId, endpointId);
        return ResponseEntity.noContent().build();
    }
}
