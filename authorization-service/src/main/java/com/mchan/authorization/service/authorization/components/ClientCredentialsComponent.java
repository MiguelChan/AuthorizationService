package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.lib.dtos.ClientCredentials;
import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ClientCredentialsMapper;
import com.mchan.authorization.service.entities.utils.SecurePasswordUtils;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues random client credentials once and retains only a peppered BCrypt hash.
 */
@Component
public class ClientCredentialsComponent {
    private final ClientCredentialsMapper mapper;
    private final SecurePasswordUtils passwords;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates credential management using existing password hashing configuration.
     */
    public ClientCredentialsComponent(ClientCredentialsMapper mapper, SecurePasswordUtils passwords) {
        this.mapper = mapper;
        this.passwords = passwords;
    }

    /**
     * Creates or rotates credentials, serializing concurrent operations on the application.
     */
    @Transactional
    public ClientCredentials issue(int applicationId) {
        requireActive(applicationId);
        ClientCredentialEntity existing = mapper.findByApplication(applicationId);
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ClientCredentialEntity credential = new ClientCredentialEntity();
        credential.setApplicationId(applicationId);
        credential.setClientId(existing == null ? UUID.randomUUID().toString() : existing.getClientId());
        try {
            credential.setSecretHash(passwords.createSecurePassword(secret));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to secure client credentials", e);
        }
        mapper.save(credential);
        return new ClientCredentials(credential.getClientId(), secret, existing == null ? 1 : existing.getVersion() + 1);
    }

    /**
     * Revokes credentials and invalidates their version.
     */
    @Transactional
    public void revoke(int applicationId) {
        requireActive(applicationId);
        mapper.revoke(applicationId);
    }

    /**
     * Authenticates a currently active application using its client identity and secret.
     */
    public ClientCredentialEntity authenticate(String clientId, String secret) {
        if (clientId == null || clientId.length() > 64 || secret == null || secret.length() != 43) {
            return null;
        }
        ClientCredentialEntity credential = mapper.findActive(clientId);
        try {
            return credential != null && passwords.isValidPassword(secret, credential.getSecretHash()) ? credential : null;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to validate client credentials", e);
        }
    }

    private void requireActive(int applicationId) {
        if (mapper.lockActiveApplication(applicationId) == null) {
            throw new InvalidArgumentException("Application must be active");
        }
    }
}
