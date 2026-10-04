package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.lib.dtos.ClientCredentials;
import com.mchan.authorization.service.authorization.components.ApplicationOwnershipComponent;
import com.mchan.authorization.service.authorization.components.ClientCredentialsComponent;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Owner-only credential rotation and revocation. Secrets are never readable later.
 */
@RestController
@RequestMapping("/api/applications/{applicationId}/credentials")
public class SpringClientCredentialsController {
    private final ClientCredentialsComponent credentials;
    private final ApplicationOwnershipComponent ownership;
    private final AuthenticationFacade authentication;

    /**
     * Creates owner-scoped credential management.
     */
    public SpringClientCredentialsController(ClientCredentialsComponent credentials, ApplicationOwnershipComponent ownership,
                                            AuthenticationFacade authentication) {
        this.credentials = credentials;
        this.ownership = ownership;
        this.authentication = authentication;
    }

    /**
     * Returns the new secret once; old credentials stop authenticating immediately.
     */
    @PostMapping("/rotate")
    public ResponseEntity<ClientCredentials> rotate(@PathVariable int applicationId) {
        requireOwner(applicationId);
        try {
            return ResponseEntity.ok().header("Cache-Control", "no-store").header("Pragma", "no-cache")
                .body(credentials.issue(applicationId));
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Revokes the current secret without returning credential material.
     */
    @DeleteMapping
    public ResponseEntity<Void> revoke(@PathVariable int applicationId) {
        requireOwner(applicationId);
        try {
            credentials.revoke(applicationId);
            return ResponseEntity.noContent().build();
        } catch (InvalidArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void requireOwner(int applicationId) {
        try {
            ownership.requireOwner(applicationId, authentication.getAuthenticationToken().getProfile().getProfileId());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
