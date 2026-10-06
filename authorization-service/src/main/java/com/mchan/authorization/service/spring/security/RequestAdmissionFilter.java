package com.mchan.authorization.service.spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Bounds dynamic admission, requests and rate-limit state before parsing or authentication.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestAdmissionFilter extends OncePerRequestFilter {
    private final int maximumBody;
    private final int globalRate;
    private final int clientRate;
    private final boolean requireHttps;
    private final Semaphore concurrent;
    private final Semaphore registrations = new Semaphore(8);
    private final BoundedTokenBucket limits = new BoundedTokenBucket(10000, System::nanoTime);

    /**
     * Validates all tunable resource bounds at startup.
     */
    public RequestAdmissionFilter(@Value("${app.limits.max-body-bytes:16384}") int maximumBody,
                                  @Value("${app.limits.max-concurrent:1024}") int maximumConcurrent,
                                  @Value("${app.limits.global-rps:5000}") int globalRate,
                                  @Value("${app.limits.client-rps:2000}") int clientRate,
                                  @Value("${app.security.require-https:true}") boolean requireHttps) {
        if (maximumBody < 1024 || maximumBody > 65536 || maximumConcurrent < 1 || maximumConcurrent > 1024
            || globalRate < 1 || globalRate > 10000 || clientRate < 1 || clientRate > 5000) {
            throw new IllegalArgumentException("Request resource configuration is outside supported bounds");
        }
        this.maximumBody = maximumBody;
        this.concurrent = new Semaphore(maximumConcurrent);
        this.globalRate = globalRate;
        this.clientRate = clientRate;
        this.requireHttps = requireHttps;
    }

    /**
     * Security chains install admission after CORS and before credential/body processing.
     */
    @org.springframework.context.annotation.Bean
    public static org.springframework.boot.web.servlet.FilterRegistrationBean<RequestAdmissionFilter> admissionRegistration(RequestAdmissionFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<RequestAdmissionFilter> registration =
            new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String path = request.getServletPath().isEmpty() ? request.getRequestURI() : request.getServletPath();
        if (requireHttps && !request.isSecure()) {
            reject(response, 400, "https_required");
            return;
        }
        if (path.startsWith("/static/") || "/favicon.ico".equals(path)) {
            chain.doFilter(request, response);
            return;
        }
        String authorization = request.getHeader("Authorization");
        if ((authorization != null && authorization.length() > 1024) || (request.getQueryString() != null
            && request.getQueryString().length() > 2048)) {
            reject(response, 400, "invalid_request");
            return;
        }
        if (!limits.allow("global", globalRate, globalRate) || !limits.allow("ip:" + request.getRemoteAddr(), 3000, 3000)) {
            reject(response, 429, "rate_limited");
            return;
        }
        if (path.startsWith("/oauth/") && authorization != null) {
            String client = clientKey(authorization);
            if ("/oauth/token".equals(path) && (!limits.allow("mint-global", 100, 200)
                || (client != null && !limits.allow("mint-client:" + client, 20, 40)))) {
                reject(response, 429, "rate_limited");
                return;
            }
            if (client != null && !limits.allow("client:" + client, clientRate, clientRate)) {
                reject(response, 429, "rate_limited");
                return;
            }
        }
        if ("/login".equals(path) && "POST".equals(request.getMethod())
            && !limits.allow("login:" + request.getRemoteAddr(), 10, 20)) {
            reject(response, 429, "rate_limited");
            return;
        }
        if ("/api/sign-up".equals(path) && !limits.allow("signup:" + request.getRemoteAddr(), 2, 10)) {
            reject(response, 429, "rate_limited");
            return;
        }
        if (!concurrent.tryAcquire()) {
            reject(response, 503, "server_busy");
            return;
        }
        boolean registrationSlot = false;
        try {
            if ("/api/sign-up".equals(path) && "POST".equals(request.getMethod())) {
                registrationSlot = registrations.tryAcquire();
                if (!registrationSlot) {
                    reject(response, 503, "server_busy");
                    return;
                }
            }
            if (request.getContentLengthLong() > maximumBody) {
                reject(response, 413, "request_too_large");
                return;
            }
            if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod()) && !"OPTIONS".equals(request.getMethod())) {
                byte[] body = request.getInputStream().readNBytes(maximumBody + 1);
                if (body.length > maximumBody) {
                    reject(response, 413, "request_too_large");
                    return;
                }
                try {
                    request = new BoundedBodyRequest(request, body);
                } catch (IllegalArgumentException e) {
                    reject(response, 400, "invalid_request");
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            if (registrationSlot) {
                registrations.release();
            }
            concurrent.release();
        }
    }

    private String clientKey(String authorization) {
        if (!authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            if (colon <= 0) {
                return null;
            }
            String client = java.net.URLDecoder.decode(decoded.substring(0, colon), StandardCharsets.UTF_8);
            return client.length() <= 64 ? client : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void reject(HttpServletResponse response, int status, String error) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        if (status == 429 || status == 503) {
            response.setHeader("Retry-After", "1");
        }
        response.getWriter().write("{\"error\":\"" + error + "\"}");
    }
}
