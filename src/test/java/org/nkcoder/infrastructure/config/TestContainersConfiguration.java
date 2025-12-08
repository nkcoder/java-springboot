package org.nkcoder.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * The @TestConfiguration with @ServiceConnection and a reusable PostgreSQL container is the modern,
 * Spring Boot 3.1+ recommended pattern for integration tests. Centralized Testcontainers
 * configuration for all integration tests.
 *
 * <p>Why this approach is recommended:
 *
 * <ul>
 *   <li><b>@TestConfiguration</b>: Spring Boot loads this only in test contexts, keeping prod code
 *       clean.
 *   <li><b>@ServiceConnection</b> (Spring Boot 3.1+): Auto-wires container connection properties
 *       (spring.datasource.url, username, password) without manual @DynamicPropertySource.
 *   <li><b>Singleton Bean</b>: Spring creates a single container instance across all test classes
 *       that import this config, avoiding repeated startup overhead.
 *   <li><b>withReuse(true)</b>: Testcontainers reuses the container across test runs (requires
 *       testcontainers.reuse.enable=true in ~/.testcontainers.properties). Speeds up local TDD.
 * </ul>
 *
 * <p>Usage: Import this config in your test classes with:
 *
 * <pre>@Import(TestContainersConfiguration.class)</pre>
 *
 * or apply via a custom meta-annotation like @IntegrationTest.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {
  private static final String POSTGRES_IMAGE = "postgres:17-alpine";

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(POSTGRES_IMAGE)
        .withDatabaseName("test-db")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);
  }
}
