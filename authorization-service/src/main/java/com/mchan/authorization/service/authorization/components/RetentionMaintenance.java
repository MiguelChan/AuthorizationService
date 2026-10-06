package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.service.authorization.dao.mappers.RetentionMapper;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs one atomic, bounded retention pass; failed passes roll back and retry after the delay.
 */
public class RetentionMaintenance {
    private static final Logger LOG = LoggerFactory.getLogger(RetentionMaintenance.class);
    private final RetentionMapper mapper;
    private final TransactionTemplate transaction;
    private final AtomicBoolean running = new AtomicBoolean();

    /**
     * Establishes an explicit deadline without depending on transactional self-invocation.
     */
    public RetentionMaintenance(RetentionMapper mapper, PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.transaction = new TransactionTemplate(manager);
        this.transaction.setTimeout(3);
    }

    /**
     * Waits ten seconds after each completed pass, on the dedicated single-thread scheduler.
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 10000, scheduler = "retentionScheduler")
    public void prune() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        long started = System.nanoTime();
        try {
            int[] deleted = transaction.execute(status -> new int[] {mapper.expiredTokens(), mapper.oldLoginHistory()});
            if (deleted != null && (deleted[0] != 0 || deleted[1] != 0)) {
                LOG.info("Retention committed: expiredTokens={}, oldLogins={}, elapsedMs={}",
                    deleted[0], deleted[1], (System.nanoTime() - started) / 1000000);
            }
        } catch (RuntimeException failure) {
            // Database messages can contain identifiers or SQL parameters. Log the category only.
            LOG.warn("Retention rolled back; retry after delay: category={}", failure.getClass().getSimpleName());
        } finally {
            running.set(false);
        }
    }
}
