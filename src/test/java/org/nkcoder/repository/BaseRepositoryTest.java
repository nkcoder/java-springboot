package org.nkcoder.repository;

import org.nkcoder.config.JpaAuditingConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

// Loads only JPA components (entities, repositories), much faster than @SpringBootTest
@DataJpaTest
@Testcontainers
// @CreatedDate and @LastModifiedDate only works when Spring Data JPA auditing is enabled
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
// Prevents Spring from replacing our PostgreSQL with H2
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BaseRepositoryTest {

  /**
   * Singleton container - NO @Container and @ServiceConnection annotation!
   *
   * <pre>
   * With the @Container annotation on a static field in a base class:
   * - Each subclass gets its own container lifecycle management
   * - Containers may start/stop unpredictably when tests run in parallel
   * - Connection errors occur when one test class's container is stopped while another is still running
   * </pre>
   */
  static PostgreSQLContainer<?> postgres;

  static {
    postgres =
        new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    postgres.start(); // Started once, never stopped until JVM exits
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
}
