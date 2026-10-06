# Supported runtime upgrade

The runtime layer uses Java 25 LTS, Spring Boot 4.1.1 (Spring Framework 7 / Spring Security 7), Gradle 9.8.0, MyBatis starter 4.1.0 and springdoc 3.1.1. Versions were checked against official Spring requirements and current release metadata. Every tracked Gradle wrapper uses the same verified distribution/wrapper SHA-256. Java configuration outside this repository is unchanged.

## Build and development

Use a Java 25 JDK. All Java modules use the Gradle toolchain; the root wrapper is the supported entry point:

```sh
./gradlew release -Pskip-functional-tests --no-daemon --max-workers=1
SPRING_PROFILES_ACTIVE=dev ./gradlew :authorization-service:bootRun --no-daemon
```

The website build uses Node 24.9.0/npm 11.6.0 and the locked dependency tree (`npm ci --legacy-peer-deps`). The legacy Create React App build still needs OpenSSL legacy-provider support in the build process. This does not change Java runtime cryptography. Frontend/build dependency modernization remains separate from this Java/Spring migration.

The historical generated TestNG functional suite is compiled but skipped by `-Pskip-functional-tests`; that flag is not evidence of an executed end-to-end suite. Local isolated HTTP/PostgreSQL/browser checks exercise the actual packaged service independently. The backend and website tests run in the release workflow.

## Production requirements

The production profile is the default. Set PORT, JDBC_DATABASE_URL, JDBC_DATABASE_USERNAME, JDBC_DATABASE_PASSWORD, SALT_VALUE, BCRYPT_ITERATIONS and OAUTH_ISSUER. Configure HTTPS/TLS termination and exact CORS_ALLOWED_ORIGINS if a separate browser origin is needed. The dev profile is explicit and uses disposable local configuration; do not deploy it publicly.

Apply existing migrations V1–V10 with the migration role before starting the application with its runtime role. The Flyway Gradle task retains explicit schema migration behavior rather than silently giving the application DDL privileges. Use separate database role credentials and verify table/sequence privileges when provisioning. Password/client hash compatibility is preserved; no data rewrite or issuer/credential rotation is hidden in the runtime migration.

## Compatibility changes and validation

Removed WebSecurityConfigurerAdapter/ant-matcher APIs are replaced by ordered SecurityFilterChain beans. OAuth remains stateless; administrator browser CSRF and session authentication remain separate. Servlet imports move to jakarta.servlet. Test mocks use MockitoBean and the modular Boot 4 WebMvc/TestRestTemplate APIs. The previous unused JPA starter is replaced by JDBC because persistence uses MyBatis. Springfox is replaced by OpenAPI 3; development documentation is at /v3/api-docs and /swagger-ui/index.html, and documentation is disabled by default in production. Existing /api and /oauth response shapes are retained.

All 178 backend tests and required service/shared-library Checkstyle pass under Java 25. The complete release passes with all 23 website tests, six snapshots, production frontend/backend packaging and legacy functional-test compilation. Fourteen isolated HTTP/PostgreSQL security checks and five actual Chrome checks pass, including registration/login/profile behavior, CSRF rejection, exact-origin CORS and explicit Basic compatibility. Final CI and later capacity evidence are recorded in the corresponding PRs. Production was not exercised.

## Primary sources

- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot 4 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Gradle Java compatibility](https://docs.gradle.org/current/userguide/compatibility.html)
- [Java LTS roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
- [MyBatis Boot compatibility](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
