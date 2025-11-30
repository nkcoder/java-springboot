package org.nkcoder.user.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class IntegrationTestingBase {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:17")
          .withDatabaseName("users")
          .withUsername("test_user")
          .withPassword("test_password0");
}
