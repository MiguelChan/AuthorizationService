package com.mchan.authorization.service.authorization.spring.controllers;

import com.mchan.authorization.service.authorization.components.ClientCredentialsComponent;
import com.mchan.authorization.service.authorization.components.OauthTokensComponent;
import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import com.mchan.authorization.service.exceptions.OauthException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth client-credentials token exchange, recipient introspection and token revocation.
 */
@RestController
@RequestMapping("/oauth")
public class SpringOauthController {
    private static final Set<String> LOOPBACK = Set.of("127.0.0.1", "::1", "0:0:0:0:0:0:0:1");
    private final ClientCredentialsComponent credentials;
    private final OauthTokensComponent tokens;
    private final boolean allowInsecureLocalhost;

    /**
     * Creates confidential-client OAuth endpoints with explicit transport configuration.
     */
    public SpringOauthController(ClientCredentialsComponent credentials, OauthTokensComponent tokens,
                                 @Value("${app.oauth.allow-insecure-localhost:false}") boolean allowInsecureLocalhost) {
        this.credentials = credentials;
        this.tokens = tokens;
        this.allowInsecureLocalhost = allowInsecureLocalhost;
    }

    /**
     * Exchanges a client secret for a scoped token addressed to one receiving application.
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> token(HttpServletRequest request) {
        return perform(() -> {
            validateForm(request, Set.of("grant_type", "audience", "scope"));
            ClientCredentialEntity source = authenticate(request);
            String grant = required(request, "grant_type");
            if (!"client_credentials".equals(grant)) {
                throw new OauthException("unsupported_grant_type", 400);
            }
            return tokens.issue(source, required(request, "audience"), request.getParameter("scope"));
        });
    }

    /**
     * Reveals live token metadata only to its authenticated recipient application.
     */
    @PostMapping("/introspect")
    public ResponseEntity<Map<String, Object>> introspect(HttpServletRequest request) {
        return perform(() -> {
            validateForm(request, Set.of("token", "token_type_hint"));
            return tokens.introspect(authenticate(request), required(request, "token"));
        });
    }

    /**
     * Revokes a source-owned token; unknown and foreign tokens also return success.
     */
    @PostMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revoke(HttpServletRequest request) {
        return perform(() -> {
            validateForm(request, Set.of("token", "token_type_hint"));
            tokens.revoke(authenticate(request), required(request, "token"));
            return Map.of();
        });
    }

    private void validateForm(HttpServletRequest request, Set<String> allowed) {
        if (!request.isSecure() && !(allowInsecureLocalhost && LOOPBACK.contains(request.getRemoteAddr())
            && LOOPBACK.contains(request.getLocalAddr()))) {
            throw new OauthException("invalid_request", 400);
        }
        try {
            if (request.getContentType() == null || !MediaType.APPLICATION_FORM_URLENCODED
                .isCompatibleWith(MediaType.parseMediaType(request.getContentType())) || request.getQueryString() != null) {
                throw new OauthException("invalid_request", 400);
            }
        } catch (IllegalArgumentException e) {
            throw new OauthException("invalid_request", 400);
        }
        for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
            if (!allowed.contains(parameter.getKey()) || parameter.getValue().length != 1) {
                throw new OauthException("invalid_request", 400);
            }
        }
    }

    private ClientCredentialEntity authenticate(HttpServletRequest request) {
        List<String> headers = Collections.list(request.getHeaders(HttpHeaders.AUTHORIZATION));
        if (headers.size() != 1 || headers.get(0).length() > 1024 || !headers.get(0).regionMatches(true, 0, "Basic ", 0, 6)) {
            throw new OauthException("invalid_client", 401);
        }
        try {
            String raw = new String(Base64.getDecoder().decode(headers.get(0).substring(6)), StandardCharsets.UTF_8);
            int colon = raw.indexOf(':');
            if (colon <= 0) {
                throw new OauthException("invalid_client", 401);
            }
            String clientId = URLDecoder.decode(raw.substring(0, colon), StandardCharsets.UTF_8);
            String secret = URLDecoder.decode(raw.substring(colon + 1), StandardCharsets.UTF_8);
            ClientCredentialEntity authenticated = credentials.authenticate(clientId, secret);
            if (authenticated == null) {
                throw new OauthException("invalid_client", 401);
            }
            return authenticated;
        } catch (IllegalArgumentException e) {
            throw new OauthException("invalid_client", 401);
        }
    }

    private String required(HttpServletRequest request, String field) {
        String value = request.getParameter(field);
        if (value == null || value.isEmpty() || value.length() > 4096) {
            throw new OauthException("invalid_request", 400);
        }
        return value;
    }

    private ResponseEntity<Map<String, Object>> perform(Supplier<Map<String, Object>> operation) {
        try {
            return response(200, operation.get());
        } catch (OauthException e) {
            return response(e.getStatus(), Map.of("error", e.getError()));
        } catch (RuntimeException e) {
            return response(500, Map.of("error", "server_error"));
        }
    }

    private ResponseEntity<Map<String, Object>> response(int status, Map<String, Object> body) {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CACHE_CONTROL, "no-store").header(HttpHeaders.PRAGMA, "no-cache");
        if (status == 401) {
            response.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"oauth\"");
        }
        return response.body(body);
    }
}
