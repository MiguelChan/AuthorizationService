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
 * Issues random client credentials once and retains only a keyed fingerprint.
 */
@Component
public class ClientCredentialsComponent {
    private final ClientCredentialsMapper mapper;
    private final SecurePasswordUtils passwords;
    private final ClientSecretHasher fingerprints;
    private final java.util.concurrent.Semaphore legacyHashing = new java.util.concurrent.Semaphore(8);
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates fingerprint verification with guarded compatibility for legacy BCrypt credentials.
     */
    public ClientCredentialsComponent(ClientCredentialsMapper mapper, SecurePasswordUtils passwords, ClientSecretHasher fingerprints) {
        this.mapper = mapper;
        this.passwords = passwords;
        this.fingerprints = fingerprints;
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
        credential.setSecretHash(fingerprints.hash(secret));
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
        if (clientId == null || clientId.length() > 64 || secret == null || !secret.matches("[A-Za-z0-9_-]{43}")) {
            return null;
        }
        ClientCredentialEntity credential = mapper.findActive(clientId);
        if (credential == null) {
            return null;
        }
        if (fingerprints.isCurrent(credential.getSecretHash())) {
            return fingerprints.matches(secret, credential.getSecretHash()) ? credential : null;
        }
        if (!legacyHashing.tryAcquire()) {
            throw new com.mchan.authorization.service.exceptions.OauthException("temporarily_unavailable", 503);
        }
        try {
            if (!passwords.isValidPassword(secret, credential.getSecretHash())) {
                return null;
            }
            String fingerprint = fingerprints.hash(secret);
            int updated = mapper.upgradeHash(credential.getApplicationId(), credential.getVersion(), credential.getSecretHash(), fingerprint);
            if (updated == 1) {
                credential.setSecretHash(fingerprint);
                return credential;
            }
            // A concurrent rotation/revocation must never be overwritten by the hash-only upgrade.
            ClientCredentialEntity current = mapper.findActive(clientId);
            return current != null && current.getVersion() == credential.getVersion()
                && fingerprints.matches(secret, current.getSecretHash()) ? current : null;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to validate client credentials", e);
        } finally {
            legacyHashing.release();
        }
    }

    private void requireActive(int applicationId) {
        if (mapper.lockActiveApplication(applicationId) == null) {
            throw new InvalidArgumentException("Application must be active");
        }
    }
}
