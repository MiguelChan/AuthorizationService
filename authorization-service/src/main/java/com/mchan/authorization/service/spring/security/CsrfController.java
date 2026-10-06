package com.mchan.authorization.service.spring.security;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Supplies the session/browser anti-forgery token before login and unsafe API calls.
 */
@RestController
public class CsrfController {
    /**
     * Returns the current browser token without permitting a cached response.
     */
    @GetMapping("/api/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
    }
}
