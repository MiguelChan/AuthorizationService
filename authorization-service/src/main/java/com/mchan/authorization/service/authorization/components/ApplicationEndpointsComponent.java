package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.lib.models.ApplicationEndpoint;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationEndpointsMapper;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Registers immutable endpoint identities and controls their metadata and lifecycle.
 */
@Component
public class ApplicationEndpointsComponent {
    private static final Set<String> METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
    private final ApplicationEndpointsMapper mapper;

    /**
     * Creates endpoint registration using catalog persistence.
     */
    public ApplicationEndpointsComponent(ApplicationEndpointsMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Creates a unique, active endpoint identity for an active application.
     */
    @Transactional
    public ApplicationEndpoint create(int appId, ApplicationEndpoint input) {
        validateIdentity(input);
        requireActiveApplication(appId);
        ApplicationEndpoint endpoint = input.toBuilder().endpointId(0).applicationId(appId).active(true)
            .description(description(input.getDescription())).build();
        try {
            mapper.create(endpoint);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Endpoint method/path or action already registered");
        }
        return endpoint;
    }

    /**
     * Lists active and inactive catalog entries for the owner.
     */
    public List<ApplicationEndpoint> list(int appId) {
        return mapper.list(appId);
    }

    /**
     * Updates display metadata while preventing grants from silently changing identity.
     */
    @Transactional
    public ApplicationEndpoint update(int appId, int endpointId, ApplicationEndpoint input) {
        if (input == null) {
            throw new InvalidArgumentException("Endpoint is required");
        }
        requireActiveApplication(appId);
        ApplicationEndpoint stored = requireEndpoint(appId, endpointId);
        if (!stored.isActive()) {
            throw new InvalidArgumentException("Endpoint must be active");
        }
        if ((input.getHttpMethod() != null && !input.getHttpMethod().equals(stored.getHttpMethod()))
            || (input.getPath() != null && !input.getPath().equals(stored.getPath()))
            || (input.getAction() != null && !input.getAction().equals(stored.getAction()))) {
            throw new InvalidArgumentException("Endpoint identity is immutable; register a replacement");
        }
        ApplicationEndpoint updated = stored.toBuilder().description(description(input.getDescription())).build();
        mapper.updateDescription(updated);
        return updated;
    }

    /**
     * Deactivates an endpoint permanently, preserving its identity for existing grants.
     */
    @Transactional
    public void deactivate(int appId, int endpointId) {
        requireActiveApplication(appId);
        requireEndpoint(appId, endpointId);
        if (mapper.deactivate(appId, endpointId) != 1) {
            throw new InvalidArgumentException("Endpoint is already inactive");
        }
    }

    private ApplicationEndpoint requireEndpoint(int appId, int endpointId) {
        ApplicationEndpoint endpoint = mapper.get(appId, endpointId);
        if (endpoint == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endpoint does not exist");
        }
        return endpoint;
    }

    private void requireActiveApplication(int appId) {
        if (mapper.lockActiveApplication(appId) == null) {
            throw new InvalidArgumentException("Application must be active");
        }
    }

    private void validateIdentity(ApplicationEndpoint input) {
        if (input == null || !METHODS.contains(input.getHttpMethod() == null ? "" : input.getHttpMethod())) {
            throw new InvalidArgumentException("Endpoint requires an uppercase HTTP method");
        }
        String path = input.getPath();
        if (path == null || path.length() > 512 || !path.startsWith("/") || path.contains("//")) {
            throw new InvalidArgumentException("Endpoint requires an absolute application path");
        }
        for (String segment : path.substring(1).split("/", -1)) {
            if (".".equals(segment) || "..".equals(segment)
                || (!segment.matches("[A-Za-z0-9._~-]*") && !segment.matches("\\{[A-Za-z][A-Za-z0-9_]*\\}"))) {
                throw new InvalidArgumentException("Endpoint path must contain literal segments or named parameters");
            }
        }
        if (input.getAction() == null || !input.getAction().matches("[a-z][a-z0-9._:-]{0,63}")) {
            throw new InvalidArgumentException("Endpoint requires a lowercase named action");
        }
    }

    private String description(String value) {
        if (value != null && value.length() > 1024) {
            throw new InvalidArgumentException("Description must be at most 1024 characters");
        }
        return value == null ? "" : value;
    }
}
