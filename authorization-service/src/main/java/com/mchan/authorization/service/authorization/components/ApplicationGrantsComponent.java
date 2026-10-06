package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.lib.models.ApplicationEndpoint;
import com.mchan.authorization.lib.models.ApplicationGrant;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationEndpointsMapper;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationGrantsMapper;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Default-deny permissions granted by the target owner; reverse access is independent.
 */
@Component
public class ApplicationGrantsComponent {
    private final ApplicationGrantsMapper mapper;
    private final ApplicationEndpointsMapper endpoints;
    private final ApplicationDao applications;

    /**
     * Creates directed permission management and evaluation.
     */
    public ApplicationGrantsComponent(ApplicationGrantsMapper mapper, ApplicationEndpointsMapper endpoints, ApplicationDao applications) {
        this.mapper = mapper;
        this.endpoints = endpoints;
        this.applications = applications;
    }

    /**
     * Creates a directional grant, or reissues a revoked grant under a new version.
     */
    @Transactional
    public ApplicationGrant create(int targetId, ApplicationGrant input) {
        if (input == null || input.getSourceApplicationId() <= 0 || input.getEndpointId() <= 0) {
            throw new InvalidArgumentException("Source application and target endpoint are required");
        }
        if (mapper.lockActiveApplication(targetId) == null) {
            throw new InvalidArgumentException("Target application must be active");
        }
        ApplicationEntity source = applications.getApplication(input.getSourceApplicationId());
        if (source == null || !source.isActive()) {
            throw new InvalidArgumentException("Source application must exist and be active");
        }
        ApplicationEndpoint endpoint = endpoints.get(targetId, input.getEndpointId());
        if (endpoint == null || !endpoint.isActive()) {
            throw new InvalidArgumentException("Endpoint must belong to the target application and be active");
        }
        ApplicationGrant grant = ApplicationGrant.builder().sourceApplicationId(source.getApplicationId())
            .targetApplicationId(targetId).endpointId(endpoint.getEndpointId()).build();
        if (mapper.create(grant) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Grant is already active");
        }
        return mapper.get(source.getApplicationId(), targetId, endpoint.getEndpointId());
    }

    /**
     * Lists incoming grants, including their revoked state, for the target owner.
     */
    public List<ApplicationGrant> list(int targetId) {
        return list(targetId, 100, 0);
    }

    /**
     * Returns a bounded keyset page instead of materializing an entire tenant catalog.
     */
    public java.util.List<ApplicationGrant> list(int targetId, int limit, int afterId) {
        if (limit < 1 || limit > 100 || afterId < 0) {
            throw new InvalidArgumentException("Page limit must be 1..100 and cursor must be nonnegative");
        }
        return mapper.list(targetId, limit, afterId);
    }

    /**
     * Revokes a target-owned grant and changes its version to invalidate prior tokens.
     */
    @Transactional
    public void revoke(int targetId, int grantId) {
        if (mapper.lockActiveApplication(targetId) == null) {
            throw new InvalidArgumentException("Target application must be active");
        }
        if (mapper.revoke(targetId, grantId) != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active grant does not exist");
        }
    }

    /**
     * Evaluates a particular direction and endpoint against current active state.
     */
    public boolean isAllowed(int sourceId, int targetId, int endpointId) {
        return mapper.allowed(sourceId, targetId).stream().anyMatch(grant -> grant.getEndpointId() == endpointId);
    }
}
