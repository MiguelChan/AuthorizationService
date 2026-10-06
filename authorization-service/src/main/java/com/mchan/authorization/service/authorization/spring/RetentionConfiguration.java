package com.mchan.authorization.service.authorization.spring;

import com.mchan.authorization.service.authorization.components.RetentionMaintenance;
import com.mchan.authorization.service.authorization.dao.mappers.RetentionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Installs the approved retention policy with an operator-controlled disable switch.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "app.retention.enabled", havingValue = "true", matchIfMissing = true)
public class RetentionConfiguration {

    /**
     * Uses one platform thread and a bounded shutdown wait regardless of virtual request threads.
     */
    @Bean
    public ThreadPoolTaskScheduler retentionScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("retention-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(4);
        return scheduler;
    }

    /**
     * Creates the scheduled worker only while the approved policy is enabled.
     */
    @Bean
    public RetentionMaintenance retentionMaintenance(RetentionMapper mapper, PlatformTransactionManager manager) {
        return new RetentionMaintenance(mapper, manager);
    }
}
