package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.authorization.dao.mappers.RetentionMapper;
import com.mchan.authorization.service.authorization.spring.RetentionConfiguration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

/** Verifies explicit disabling and non-overlapping retention transactions. */
public class RetentionMaintenanceTests {
    @Test
    public void disabledPolicy_should_notInstallWorkerOrScheduler() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of("app.retention.enabled", "false")));
            context.register(RetentionConfiguration.class);
            context.refresh();
            assertThat(context.getBeansOfType(RetentionMaintenance.class)).isEmpty();
            assertThat(context.containsBean("retentionScheduler")).isFalse();
        }
    }

    @Test
    public void concurrentInvocation_should_skipWithoutStartingAnotherTransaction() throws Exception {
        var mapper = mock(RetentionMapper.class);
        var manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenAnswer(call -> {
            assertThat(call.getArgument(0, TransactionDefinition.class).getTimeout()).isEqualTo(3);
            return new SimpleTransactionStatus();
        });
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        when(mapper.expiredTokens()).thenAnswer(call -> {
            entered.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Test release timeout");
            }
            return 1;
        });
        var job = new RetentionMaintenance(mapper, manager);
        Thread worker = Thread.ofPlatform().start(job::prune);
        try {
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            job.prune();
            verify(manager, times(1)).getTransaction(any());
        } finally {
            release.countDown();
            worker.join(5000);
            assertThat(worker.isAlive()).isFalse();
        }
        job.prune();
        verify(manager, times(2)).commit(any());
    }
}
