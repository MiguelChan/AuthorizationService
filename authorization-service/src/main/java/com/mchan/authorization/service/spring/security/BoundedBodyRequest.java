package com.mchan.authorization.service.spring.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replays one bounded body for JSON and servlet form consumers, including chunked uploads.
 */
public class BoundedBodyRequest extends HttpServletRequestWrapper {
    private final byte[] body;
    private final Map<String, String[]> parameters;

    /**
     * Preserves bounded form/query parameters after the admission filter reads the stream.
     */
    public BoundedBodyRequest(HttpServletRequest request, byte[] body) {
        super(request);
        this.body = body;
        Map<String, String[]> parsed = new LinkedHashMap<>();
        if (body.length == 0) {
            request.getParameterMap().forEach((key, values) -> parsed.put(key, values.clone()));
            if (parsed.size() > 64 || parsed.values().stream().anyMatch(values -> values.length > 64)) {
                throw new IllegalArgumentException("Too many request parameters");
            }
        } else {
            parse(request.getQueryString(), parsed);
        }
        if (request.getContentType() != null && request.getContentType().startsWith("application/x-www-form-urlencoded")) {
            parse(new String(body, StandardCharsets.UTF_8), parsed);
        }
        this.parameters = Collections.unmodifiableMap(parsed);
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream stream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                return stream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new IllegalStateException("Bounded synchronous body replay has no asynchronous read listener");
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = parameters.get(name);
        return values == null ? null : values.clone();
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        return values == null ? null : values[0];
    }

    private static void parse(String encoded, Map<String, String[]> parsed) {
        if (encoded == null || encoded.isEmpty()) {
            return;
        }
        String[] parts = encoded.split("&", -1);
        if (parts.length > 64) {
            throw new IllegalArgumentException("Too many request parameters");
        }
        for (String part : parts) {
            String[] pair = part.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.length == 2 ? pair[1] : "", StandardCharsets.UTF_8);
            String[] existing = parsed.get(key);
            if (existing == null) {
                parsed.put(key, new String[] {value});
            } else {
                if (existing.length >= 64) {
                    throw new IllegalArgumentException("Too many parameter values");
                }
                String[] values = java.util.Arrays.copyOf(existing, existing.length + 1);
                values[existing.length] = value;
                parsed.put(key, values);
            }
        }
        if (parsed.size() > 64) {
            throw new IllegalArgumentException("Too many request parameters");
        }
    }
}
