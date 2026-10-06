package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;

import com.mchan.authorization.service.authorization.dao.mappers.RetentionMapper;
import com.mchan.authorization.service.authorization.spring.RetentionConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Tests require the disposable database created by tools/validation/retention.py. */
public class RetentionDatabaseTests {
    private HikariDataSource dataSource;
    private JdbcTemplate admin;
    private JdbcTemplate runtime;
    private RetentionMapper mapper;
    private JdbcTransactionManager manager;
    private RetentionMaintenance job;

    /** Creates a real DML-only mapper and isolated fixtures. */
    @BeforeEach
    public void setUp() throws Exception {
        String url = System.getenv("RETENTION_TEST_URL");
        assertThat(url).startsWith("jdbc:postgresql://127.0.0.1:").contains("/retention_probe");
        admin = new JdbcTemplate(new DriverManagerDataSource(url, "probe", "fixture-only"));
        admin.execute("TRUNCATE auth_db.profiles CASCADE");
        admin.execute("INSERT INTO auth_db.profiles VALUES('profile','First','Last','retention@example.com','1234567890')");
        admin.execute("INSERT INTO auth_db.accounts VALUES('account','profile','CLASSIC','retention@example.com','fixture')");
        admin.execute("INSERT INTO auth_db.applications(application_id,profile_id,app_name,short_description,redirect_url,is_active) "
            + "VALUES(1,'profile','source','source','https://source.example',true),(2,'profile','target','target','https://target.example',true)");
        admin.execute("INSERT INTO auth_db.client_credentials(application_id,client_id,secret_hash) VALUES(1,'source','fixture'),(2,'target','fixture')");
        admin.execute("INSERT INTO auth_db.application_endpoints(endpoint_id,application_id,http_method,path,action) "
            + "SELECT n,2,'GET','/route/'||n,'action.'||n FROM generate_series(1,64) n");
        admin.execute("INSERT INTO auth_db.application_grants(grant_id,source_application_id,target_application_id,endpoint_id) "
            + "SELECT n,1,2,n FROM generate_series(1,64) n");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername("auth_runtime");
        config.setPassword("runtime-fixture-only");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(1000);
        config.setValidationTimeout(500);
        config.addDataSourceProperty("socketTimeout", "3");
        config.addDataSourceProperty("options", "-c statement_timeout=2000 -c lock_timeout=500");
        dataSource = new HikariDataSource(config);
        runtime = new JdbcTemplate(dataSource);
        Configuration configuration = new Configuration();
        configuration.addMapper(RetentionMapper.class);
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(configuration);
        mapper = new SqlSessionTemplate(factory.getObject()).getMapper(RetentionMapper.class);
        manager = new JdbcTransactionManager(dataSource);
        job = new RetentionMaintenance(mapper, manager);
    }

    /** Closes owned pool threads and removes timeout fixtures. */
    @AfterEach
    public void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (admin != null) {
            admin.execute("DROP TRIGGER IF EXISTS slow_retention ON auth_db.sessions");
            admin.execute("DROP FUNCTION IF EXISTS auth_db.slow_retention()");
        }
    }

    private void tokens(int count, boolean expired) {
        admin.execute("INSERT INTO auth_db.oauth_tokens "
            + "SELECT lpad(n::text,64,'0'),1,2,1,1,'https://issuer.example',CURRENT_TIMESTAMP-INTERVAL '1 day',"
            + (expired ? "CURRENT_TIMESTAMP-INTERVAL '1 hour'" : "CURRENT_TIMESTAMP+INTERVAL '1 day'")
            + ",NULL FROM generate_series(1," + count + ") n");
        admin.execute("INSERT INTO auth_db.oauth_token_permissions SELECT token_hash,n,1 FROM auth_db.oauth_tokens CROSS JOIN generate_series(1,64) n");
    }

    private void history(int count) {
        admin.execute("INSERT INTO auth_db.sessions(account_id,session_type,session_time) "
            + "SELECT 'account','CLASSIC',CURRENT_TIMESTAMP-INTERVAL '31 days' FROM generate_series(1," + count + ") n");
    }

    private int count(String table) {
        return admin.queryForObject("SELECT count(*) FROM auth_db." + table, Integer.class);
    }

    @Test
    public void maximumBatches_should_cascade64PermissionsAndPreserveLiveAuthorizationState() {
        tokens(2001, true);
        admin.execute("INSERT INTO auth_db.oauth_tokens SELECT repeat('f',64),1,2,1,1,'https://issuer.example',"
            + "CURRENT_TIMESTAMP,CURRENT_TIMESTAMP+INTERVAL '1 day',NULL");
        admin.execute("INSERT INTO auth_db.oauth_token_permissions SELECT repeat('f',64),n,1 FROM generate_series(1,64) n");
        history(1001);
        admin.execute("INSERT INTO auth_db.sessions(account_id,session_type,session_time) VALUES('account','CLASSIC',CURRENT_TIMESTAMP)");
        Instant start = Instant.now();
        job.prune();
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        System.out.println("Retention maximum batch (128000 cascading permissions): " + elapsed + "ms");
        assertThat(elapsed).isLessThan(3000);
        assertThat(count("oauth_tokens")).isEqualTo(2);
        assertThat(count("oauth_token_permissions")).isEqualTo(128);
        assertThat(count("sessions")).isEqualTo(2);
        job.prune();
        assertThat(count("oauth_tokens")).isEqualTo(1);
        assertThat(count("oauth_token_permissions")).isEqualTo(64);
        assertThat(count("sessions")).isEqualTo(1);
        assertThat(count("profiles")).isEqualTo(1);
        assertThat(count("accounts")).isEqualTo(1);
        assertThat(count("applications")).isEqualTo(2);
        assertThat(count("client_credentials")).isEqualTo(2);
        assertThat(count("application_grants")).isEqualTo(64);
        assertThat(count("application_endpoints")).isEqualTo(64);
        assertThat(admin.queryForObject("SELECT sum(version) FROM auth_db.client_credentials", Integer.class)).isEqualTo(2);
        assertThat(admin.queryForObject("SELECT sum(version) FROM auth_db.application_grants", Integer.class)).isEqualTo(64);
    }

    @Test
    public void boundary_should_deleteExpiredTokensAndOnlyStrictlyOlderLogins() {
        new TransactionTemplate(manager).executeWithoutResult(status -> {
            runtime.execute("INSERT INTO auth_db.oauth_tokens SELECT repeat('a',64),1,2,1,1,'https://issuer.example',"
                + "CURRENT_TIMESTAMP-INTERVAL '1 day',CURRENT_TIMESTAMP,NULL");
            runtime.execute("INSERT INTO auth_db.sessions(account_id,session_type,session_time) VALUES"
                + "('account','CLASSIC',CURRENT_TIMESTAMP-INTERVAL '30 days'),"
                + "('account','CLASSIC',CURRENT_TIMESTAMP-INTERVAL '30 days 1 microsecond')");
            assertThat(mapper.expiredTokens()).isEqualTo(1);
            assertThat(mapper.oldLoginHistory()).isEqualTo(1);
            assertThat(runtime.queryForObject("SELECT count(*) FROM auth_db.sessions", Integer.class)).isEqualTo(1);
        });
    }

    @Test
    public void lockedRows_should_beSkippedThenDrainedAfterTheirTransactionEnds() throws Exception {
        tokens(2, true);
        history(2);
        try (Connection lock = admin.getDataSource().getConnection()) {
            lock.setAutoCommit(false);
            try (var statement = lock.createStatement()) {
                statement.execute("SELECT token_hash FROM auth_db.oauth_tokens ORDER BY token_hash LIMIT 1 FOR UPDATE");
                statement.execute("SELECT session_id FROM auth_db.sessions ORDER BY session_id LIMIT 1 FOR UPDATE");
                job.prune();
                assertThat(count("oauth_tokens")).isEqualTo(1);
                assertThat(count("oauth_token_permissions")).isEqualTo(64);
                assertThat(count("sessions")).isEqualTo(1);
            } finally {
                lock.rollback();
            }
        }
        job.prune();
        assertThat(count("oauth_tokens")).isZero();
        assertThat(count("oauth_token_permissions")).isZero();
        assertThat(count("sessions")).isZero();
    }

    @Test
    public void timedOutHistory_should_rollBackTokensAndRecoverOnTheNextPass() {
        tokens(1, true);
        history(1);
        admin.execute("CREATE FUNCTION auth_db.slow_retention() RETURNS trigger LANGUAGE plpgsql AS "
            + "$$ BEGIN PERFORM pg_sleep(5); RETURN OLD; END $$");
        admin.execute("CREATE TRIGGER slow_retention BEFORE DELETE ON auth_db.sessions FOR EACH ROW EXECUTE FUNCTION auth_db.slow_retention()");
        Instant start = Instant.now();
        job.prune();
        assertThat(Duration.between(start, Instant.now()).toMillis()).isLessThan(4000);
        assertThat(count("oauth_tokens")).isEqualTo(1);
        assertThat(count("oauth_token_permissions")).isEqualTo(64);
        assertThat(count("sessions")).isEqualTo(1);
        admin.execute("DROP TRIGGER slow_retention ON auth_db.sessions");
        job.prune();
        assertThat(count("oauth_tokens")).isZero();
        assertThat(count("sessions")).isZero();
    }

    @Test
    public void disabledScheduler_should_preserveEligibleRowsBeyondTheInitialDelay() throws Exception {
        tokens(1, true);
        history(1);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of("app.retention.enabled", "false")));
            context.registerBean(RetentionMapper.class, () -> mapper);
            context.registerBean(PlatformTransactionManager.class, () -> manager);
            context.register(RetentionConfiguration.class);
            context.refresh();
            assertThat(context.getBeansOfType(RetentionMaintenance.class)).isEmpty();
            Thread.sleep(11000);
            assertThat(count("oauth_tokens")).isEqualTo(1);
            assertThat(count("oauth_token_permissions")).isEqualTo(64);
            assertThat(count("sessions")).isEqualTo(1);
        }
    }

    @Test
    public void actualScheduler_should_pruneAutomaticallyAndStopOnContextClose() throws Exception {
        tokens(1, true);
        history(1);
        ThreadPoolTaskScheduler scheduler;
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(RetentionMapper.class, () -> mapper);
            context.registerBean(PlatformTransactionManager.class, () -> manager);
            context.register(RetentionConfiguration.class);
            context.refresh();
            scheduler = context.getBean(ThreadPoolTaskScheduler.class);
            assertThat(scheduler.getPoolSize()).isEqualTo(1);
            assertThat(count("oauth_tokens")).isEqualTo(1);
            Instant deadline = Instant.now().plusSeconds(15);
            while (count("oauth_tokens") != 0 && Instant.now().isBefore(deadline)) {
                Thread.sleep(100);
            }
            assertThat(count("oauth_tokens")).isZero();
            assertThat(count("sessions")).isZero();
        }
        assertThat(scheduler.getScheduledThreadPoolExecutor().isTerminated()).isTrue();
    }
}
