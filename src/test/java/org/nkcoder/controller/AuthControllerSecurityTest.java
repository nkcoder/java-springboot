package org.nkcoder.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.AuthTokens;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.enums.Role;
import org.nkcoder.service.AuthService;
import org.nkcoder.service.UserService;
import org.nkcoder.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Use @SpringBootTest to load the full context to test the authentication of endpoints.
 * Because @WebMvcTest won't load SecurityConfig and JwtAuthenticationEntryPoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Security Tests")
@ActiveProfiles("test")
public class AuthControllerSecurityTest {

  // Singleton container for database (needed by JPA)
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

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtUtil jwtUtil;

  @Nested
  @DisplayName("Public Endpoints")
  class PublicEndpoints {

    @Test
    @DisplayName("register endpoint is accessible without authentication")
    void registerIsPublic() throws Exception {
      AuthResponse authResponse = createAuthResponse();
      given(authService.register(any())).willReturn(authResponse);

      mockMvc
          .perform(
              post("/api/users/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                      "email": "test@example.com",
                       "password": "Password123",
                       "name": "Test User",
                       "role": "MEMBER"
                      }
                      """))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("login endpoint is accessible without authentication")
    void loginIsPublic() throws Exception {
      AuthResponse response = createAuthResponse();
      given(authService.login(any())).willReturn(response);

      mockMvc
          .perform(
              post("/api/users/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "email": "test@example.com",
                        "password": "Password123"
                      }
                      """))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("refresh endpoint is accessible without authentication")
    void refreshIsPublic() throws Exception {
      AuthResponse response = createAuthResponse();
      given(authService.refreshTokens(any())).willReturn(response);

      mockMvc
          .perform(
              post("/api/users/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "refreshToken": "some-refresh-token"
                      }
                      """))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("Protected Endpoints")
  class ProtectedEndpoints {

    @Test
    @DisplayName("GET /api/users/me returns 401 without token")
    void getMeRequiresAuth() throws Exception {
      mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/users/me returns 401 without token")
    void updateMeRequiresAuth() throws Exception {
      mockMvc
          .perform(
              patch("/api/users/me")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                       {"name": "New Name"}
                      """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/users/me/password returns 401 without token")
    void changePasswordRequiresAuth() throws Exception {
      mockMvc
          .perform(
              patch("/api/users/me/password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "currentPassword": "Old123",
                        "newPassword": "New123",
                        "confirmPassword": "New123"
                      }
                      """))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Admin Endpoints")
  class AdminEndpoints {

    @Test
    @DisplayName("GET /api/users/{id} returns 401 without token")
    void getUserByIdRequiresAuth() throws Exception {
      mockMvc
          .perform(get("/api/users/{userId}", UUID.randomUUID()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/users/{id} returns 401 without token")
    void updateUserRequiresAuth() throws Exception {
      mockMvc
          .perform(
              patch("/api/users/{userId}", UUID.randomUUID())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"name": "Admin Update"}
                      """))
          .andExpect(status().isUnauthorized());
    }
  }

  private AuthResponse createAuthResponse() {
    return new AuthResponse(
        new UserResponse(
            UUID.randomUUID(),
            "test@example.com",
            "Test User",
            Role.MEMBER,
            false,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now()),
        new AuthTokens("access-token", "refresh-token"));
  }
}
