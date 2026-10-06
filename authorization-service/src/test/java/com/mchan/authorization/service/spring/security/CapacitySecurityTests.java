package com.mchan.authorization.service.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mchan.authorization.service.authorization.components.ClientSecretHasher;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Exercises adversarial request, rate-state, hashing and overload bounds with real consumers.
 */
public class CapacitySecurityTests {
    @Test
    public void generatedSecrets_should_beVersionedKeyedAndConstantLength() {
        String secret = "A".repeat(43);
        ClientSecretHasher hashes = new ClientSecretHasher("isolated-pepper");
        String stored = hashes.hash(secret);
        assertThat(stored).startsWith("hmac-sha256$").doesNotContain(secret);
        assertThat(hashes.matches(secret, stored)).isTrue();
        assertThat(hashes.matches("B".repeat(43), stored)).isFalse();
        assertThat(new ClientSecretHasher("other-pepper").matches(secret, stored)).isFalse();
        assertThat(hashes.isCurrent("$2a$11$legacy")).isFalse();
        assertThatThrownBy(() -> hashes.hash("human-password")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rateState_should_notResetActiveLimitsByEviction() {
        AtomicLong clock = new AtomicLong();
        BoundedTokenBucket buckets = new BoundedTokenBucket(2, clock::get);
        assertThat(buckets.allow("a", 1, 1)).isTrue();
        assertThat(buckets.allow("a", 1, 1)).isFalse();
        assertThat(buckets.allow("b", 1, 1)).isTrue();
        assertThat(buckets.allow("c", 1, 1)).isFalse();
        assertThat(buckets.size()).isEqualTo(2);
        assertThat(buckets.allow("a", 1, 1)).isFalse();
        clock.set(61000000000L);
        assertThat(buckets.allow("c", 1, 1)).isTrue();
        assertThat(buckets.size()).isLessThanOrEqualTo(2);
    }

    @Test
    public void failedLogins_should_blockBeforeWorkAndRecoverAfterCooldown() {
        AtomicLong clock = new AtomicLong();
        LoginAttemptGuard guard = new LoginAttemptGuard(1, clock::get);
        for (int i = 0; i < 5; i++) {
            guard.enter("user@example.com", "wrong");
            guard.failed("user@example.com");
            guard.release();
        }
        assertThatThrownBy(() -> guard.enter("USER@example.com", "right"))
            .isInstanceOf(BadCredentialsException.class);
        clock.set(61000000000L);
        guard.enter("user@example.com", "right");
        guard.succeeded("user@example.com");
        guard.release();
    }

    @Test
    public void activePasswordWork_should_haveNoUnboundedWaitQueue() {
        LoginAttemptGuard guard = new LoginAttemptGuard(1, System::nanoTime);
        guard.enter("first@example.com", "first");
        assertThatThrownBy(() -> guard.enter("second@example.com", "second"))
            .hasMessage("Authentication temporarily unavailable");
        guard.release();
        guard.enter("second@example.com", "second");
        guard.release();
    }

    @Test
    public void plaintextAndForwardedHeaders_should_notBypassTransport() throws Exception {
        RequestAdmissionFilter filter = new RequestAdmissionFilter(1024, 1, 10, 10, true);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth/token");
        request.addHeader("X-Forwarded-Proto", "https");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("Transport bypass");
        });
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("https_required");
    }

    @Test
    public void declaredAndUnknownLengthBodies_should_beRejectedBeforeParsing() throws Exception {
        RequestAdmissionFilter filter = new RequestAdmissionFilter(1024, 1, 100, 100, false);
        MockHttpServletRequest declared = new MockHttpServletRequest("POST", "/api/sign-up");
        declared.setContent(new byte[1025]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(declared, response, (req, res) -> {
            throw new AssertionError("Oversized declared body executed");
        });
        assertThat(response.getStatus()).isEqualTo(413);
        MockHttpServletRequest chunked = new MockHttpServletRequest("POST", "/api/sign-up") {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        chunked.setContent(new byte[1025]);
        response = new MockHttpServletResponse();
        filter.doFilter(chunked, response, (req, res) -> {
            throw new AssertionError("Oversized chunked body executed");
        });
        assertThat(response.getStatus()).isEqualTo(413);
    }

    @Test
    public void replay_should_preserveDuplicateFormsAndRejectMalformedEncoding() throws Exception {
        MockHttpServletRequest source = new MockHttpServletRequest("POST", "/oauth/token");
        source.setContentType("application/x-www-form-urlencoded");
        BoundedBodyRequest replay = new BoundedBodyRequest(source, "scope=read&scope=write".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(replay.getParameterValues("scope")).containsExactly("read", "write");
        assertThat(replay.getInputStream().readAllBytes()).isEqualTo("scope=read&scope=write".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThatThrownBy(() -> new BoundedBodyRequest(source, "token=%ZZ".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void concurrentLimit_should_releaseOnlyOwnedSlotsAfterCompletion() throws Exception {
        RequestAdmissionFilter filter = new RequestAdmissionFilter(1024, 1, 100, 100, false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var active = executor.submit(() -> {
                try {
                    filter.doFilter(new MockHttpServletRequest("GET", "/api/ping"), new MockHttpServletResponse(), (req, res) -> {
                        entered.countDown();
                        try {
                            release.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
                MockHttpServletResponse rejected = new MockHttpServletResponse();
                filter.doFilter(new MockHttpServletRequest("GET", "/api/ping"), rejected, (req, res) -> {
                    throw new AssertionError("Concurrency exceeded");
                });
                assertThat(rejected.getStatus()).isEqualTo(503);
                assertThat(rejected.getHeader("Retry-After")).isEqualTo("1");
            } finally {
                release.countDown();
            }
            active.get(2, TimeUnit.SECONDS);
        }
        MockHttpServletResponse next = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/ping"), next, (req, res) -> res.getWriter().write("ok"));
        assertThat(next.getContentAsString()).isEqualTo("ok");
    }

    @Test
    public void spoofedProxyAddresses_should_notResetTheSourceIpRate() throws Exception {
        RequestAdmissionFilter filter = new RequestAdmissionFilter(1024, 10, 10000, 5000, false);
        for (int i = 0; i < 21; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
            request.addHeader("X-Forwarded-For", "203.0.113." + i);
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> {});
            if (i == 20) {
                assertThat(response.getStatus()).isEqualTo(429);
                assertThat(response.getHeader("Retry-After")).isEqualTo("1");
            }
        }
    }

    @Test
    public void distributedRegistration_should_notStartUnboundedPasswordHashes() throws Exception {
        RequestAdmissionFilter filter = new RequestAdmissionFilter(1024, 100, 10000, 5000, false);
        CountDownLatch entered = new CountDownLatch(8);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            java.util.List<java.util.concurrent.Future<?>> active = new java.util.ArrayList<>();
            for (int i = 0; i < 8; i++) {
                final int peer = i;
                active.add(executor.submit(() -> {
                    try {
                        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sign-up");
                        request.setRemoteAddr("203.0.113." + peer);
                        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                            entered.countDown();
                            try {
                                release.await(5, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
            try {
                assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
                MockHttpServletRequest ninth = new MockHttpServletRequest("POST", "/api/sign-up");
                ninth.setRemoteAddr("203.0.113.99");
                MockHttpServletResponse response = new MockHttpServletResponse();
                filter.doFilter(ninth, response, (req, res) -> {
                    throw new AssertionError("Distributed signup exceeded the password-work budget");
                });
                assertThat(response.getStatus()).isEqualTo(503);
                assertThat(response.getHeader("Retry-After")).isEqualTo("1");
            } finally {
                release.countDown();
            }
            for (var task : active) {
                task.get(2, TimeUnit.SECONDS);
            }
        }
        MockHttpServletResponse next = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/api/sign-up"), next, (req, res) -> res.getWriter().write("ready"));
        assertThat(next.getContentAsString()).isEqualTo("ready");
    }
}
