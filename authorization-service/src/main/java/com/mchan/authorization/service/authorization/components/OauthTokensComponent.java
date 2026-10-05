package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.lib.models.ApplicationGrant;
import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import com.mchan.authorization.service.authorization.dao.entities.OauthPermissionEntity;
import com.mchan.authorization.service.authorization.dao.entities.OauthTokenEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationGrantsMapper;
import com.mchan.authorization.service.authorization.dao.mappers.ClientCredentialsMapper;
import com.mchan.authorization.service.authorization.dao.mappers.OauthTokensMapper;
import com.mchan.authorization.service.exceptions.OauthException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues audience-bound opaque tokens and evaluates every token against current authorization state.
 */
@Component
public class OauthTokensComponent {
    private final OauthTokensMapper tokens;
    private final ApplicationGrantsMapper grants;
    private final ClientCredentialsMapper clients;
    private final String issuer;
    private final int ttl;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates short-lived service-to-service token issuance and validation.
     */
    public OauthTokensComponent(OauthTokensMapper tokens, ApplicationGrantsMapper grants, ClientCredentialsMapper clients,
                                @Value("${app.oauth.issuer}") String issuer,
                                @Value("${app.oauth.token-ttl-seconds:300}") int ttl) {
        URI uri = URI.create(issuer);
        boolean localHttp = "http".equals(uri.getScheme()) && uri.getHost() != null && Set.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost());
        if (issuer.length() > 512 || uri.getHost() == null || (!"https".equals(uri.getScheme()) && !localHttp)
            || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null || ttl < 1 || ttl > 3600) {
            throw new IllegalArgumentException("Configure an absolute HTTPS OAuth issuer and a TTL between 1 and 3600 seconds");
        }
        this.tokens = tokens;
        this.grants = grants;
        this.clients = clients;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    /**
     * Issues only currently granted scopes for the requested target client audience.
     */
    @Transactional
    public Map<String, Object> issue(ClientCredentialEntity source, String audience, String scope) {
        if (audience == null || audience.isEmpty() || audience.length() > 64) {
            throw new OauthException("invalid_request", 400);
        }
        ClientCredentialEntity target = clients.findActive(audience);
        if (target == null) {
            throw new OauthException("invalid_target", 400);
        }
        List<ApplicationGrant> allowed = grants.allowed(source.getApplicationId(), target.getApplicationId());
        Set<String> available = allowed.stream().map(ApplicationGrant::getAction).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> requested = available;
        if (scope != null) {
            if (scope.isEmpty() || scope.length() > 4096 || !scope.matches("[a-z][a-z0-9._:-]*( [a-z][a-z0-9._:-]*)*")) {
                throw new OauthException("invalid_scope", 400);
            }
            requested = new LinkedHashSet<>(Arrays.asList(scope.split(" ")));
        }
        if (requested.isEmpty() || requested.size() > 64 || !available.containsAll(requested)) {
            throw new OauthException("invalid_scope", 400);
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        OauthTokenEntity token = new OauthTokenEntity();
        token.setTokenHash(hash(value));
        token.setSourceId(source.getApplicationId());
        token.setTargetId(target.getApplicationId());
        token.setSourceCredentialVersion(source.getVersion());
        token.setTargetCredentialVersion(target.getVersion());
        token.setIssuer(issuer);
        Instant now = Instant.now();
        token.setIssuedAt(Date.from(now));
        token.setExpiresAt(Date.from(now.plusSeconds(ttl)));
        tokens.create(token);
        for (ApplicationGrant grant : allowed) {
            if (requested.contains(grant.getAction())) {
                tokens.addPermission(token.getTokenHash(), grant.getGrantId(), grant.getVersion());
            }
        }
        return Map.of("access_token", value, "token_type", "Bearer", "expires_in", ttl, "scope", String.join(" ", requested));
    }

    /**
     * Returns metadata only to the authenticated recipient, rechecking all live permissions.
     */
    public Map<String, Object> introspect(ClientCredentialEntity recipient, String value) {
        if (!validToken(value)) {
            return Map.of("active", false);
        }
        String hash = hash(value);
        OauthTokenEntity token = tokens.findActive(hash);
        if (token == null || token.getTargetId() != recipient.getApplicationId() || !issuer.equals(token.getIssuer())
            || token.getTargetCredentialVersion() != recipient.getVersion() || !token.getExpiresAt().after(new Date())) {
            return Map.of("active", false);
        }
        List<OauthPermissionEntity> permitted = tokens.permissions(hash);
        if (permitted.isEmpty()) {
            return Map.of("active", false);
        }
        List<Map<String, Object>> permissions = new ArrayList<>();
        for (OauthPermissionEntity permission : permitted) {
            permissions.add(Map.of("endpoint_id", permission.getEndpointId(), "action", permission.getAction(),
                "http_method", permission.getHttpMethod(), "path", permission.getPath()));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("active", true);
        result.put("client_id", tokens.clientId(token.getSourceId()));
        result.put("sub", Integer.toString(token.getSourceId()));
        result.put("aud", recipient.getClientId());
        result.put("iss", issuer);
        result.put("iat", token.getIssuedAt().toInstant().getEpochSecond());
        result.put("exp", token.getExpiresAt().toInstant().getEpochSecond());
        result.put("scope", permitted.stream().map(OauthPermissionEntity::getAction).collect(Collectors.joining(" ")));
        result.put("permissions", permissions);
        return result;
    }

    /**
     * Revokes a source-owned token without exposing unknown or foreign token state.
     */
    public void revoke(ClientCredentialEntity source, String value) {
        if (validToken(value)) {
            tokens.revoke(hash(value), source.getApplicationId());
        }
    }

    private boolean validToken(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{43}");
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) {
                result.append(String.format("%02x", b & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Token hashing is unavailable", e);
        }
    }
}
