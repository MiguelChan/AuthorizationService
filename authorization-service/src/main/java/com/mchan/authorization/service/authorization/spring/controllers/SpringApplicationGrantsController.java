package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.lib.models.ApplicationGrant;
import com.mchan.authorization.service.authorization.components.ApplicationGrantsComponent;
import com.mchan.authorization.service.authorization.components.ApplicationManagementAccess;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Only the receiving application's owner can grant or revoke incoming access.
 */
@RestController
@RequestMapping("/api/applications/{targetId}/grants")
public class SpringApplicationGrantsController {
    private final ApplicationGrantsComponent grants;
    private final ApplicationManagementAccess access;

    /**
     * Creates target-owner-controlled permission management.
     */
    public SpringApplicationGrantsController(ApplicationGrantsComponent grants, ApplicationManagementAccess access) {
        this.grants = grants;
        this.access = access;
    }

    /**
     * Grants one source application permission for one target endpoint.
     */
    @PostMapping
    public ApplicationGrant create(@PathVariable int targetId, @RequestBody ApplicationGrant grant) {
        access.requireOwner(targetId);
        return grants.create(targetId, grant);
    }

    /**
     * Lists incoming target-owned grants.
     */
    @GetMapping
    public List<ApplicationGrant> list(@PathVariable int targetId,
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "100") int limit,
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int afterId) {
        access.requireOwner(targetId);
        return grants.list(targetId, limit, afterId);
    }

    /**
     * Revokes one target-owned direction without altering reverse access.
     */
    @DeleteMapping("/{grantId}")
    public ResponseEntity<Void> revoke(@PathVariable int targetId, @PathVariable int grantId) {
        access.requireOwner(targetId);
        grants.revoke(targetId, grantId);
        return ResponseEntity.noContent().build();
    }
}
