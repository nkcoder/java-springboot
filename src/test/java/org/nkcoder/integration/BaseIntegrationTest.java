package org.nkcoder.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.nkcoder.repository.RefreshTokenRepository;
import org.nkcoder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BaseIntegrationTest {
  // Singleton container - shared across all integration tests
  static PostgreSQLContainer<?> postgres;

  static {
    postgres =
        new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @LocalServerPort protected int port;

  @Autowired protected UserRepository userRepository;
  @Autowired protected RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void setupRestAssured() {
    RestAssured.port = port;
    RestAssured.basePath = "/api/users";
  }

  @BeforeEach
  void cleanDatabase() {
    userRepository.deleteAll();
    refreshTokenRepository.deleteAll();
  }
}
