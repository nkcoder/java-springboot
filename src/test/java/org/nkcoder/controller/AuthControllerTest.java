package org.nkcoder.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.nkcoder.dto.auth.*;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.enums.Role;
import org.nkcoder.security.JwtAuthenticationFilter;
import org.nkcoder.service.AuthService;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("AuthController tests")
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {JwtAuthenticationFilter.class})
    })
class AuthControllerTest extends BaseControllerTest {
  @MockitoBean private AuthService authService;

  @Nested
  @DisplayName("Registration Tests")
  class RegistrationTests {

    @Test
    @DisplayName("Should register user successfully with valid request")
    void shouldRegisterUserSuccessfully() throws Exception {
      // Given
      RegisterRequest request =
          new RegisterRequest("test@example.com", "Password@123!", "John Doe", Role.MEMBER);

      AuthResponse authResponse =
          new AuthResponse(
              new UserResponse(
                  UUID.randomUUID(),
                  "test@example.com",
                  "John Doe",
                  Role.MEMBER,
                  false,
                  LocalDateTime.now(),
                  LocalDateTime.now(),
                  LocalDateTime.now()),
              new AuthTokens("test-access-token", "test-refresh-token"));

      given(authService.register(any(RegisterRequest.class))).willReturn(authResponse);

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.message").value("User registered successfully"))
          .andExpect(jsonPath("$.data.tokens.accessToken").value("test-access-token"))
          .andExpect(jsonPath("$.data.tokens.refreshToken").value("test-refresh-token"))
          .andExpect(jsonPath("$.data.user.id").exists());

      verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when register request is invalid")
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
      // Given
      RegisterRequest request = new RegisterRequest(null, "test-pass", "John Doe", Role.MEMBER);
      // Missing required fields

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should pass correct request to service")
    void shouldPassCorrectRequestToService() throws Exception {
      // Given
      RegisterRequest request =
          new RegisterRequest("test@example.com", "Password@123!", "John Doe", Role.MEMBER);

      AuthResponse authResponse =
          new AuthResponse(
              new UserResponse(
                  UUID.randomUUID(),
                  "test@example.com",
                  "test",
                  Role.MEMBER,
                  true,
                  LocalDateTime.now(),
                  LocalDateTime.now(),
                  LocalDateTime.now()),
              new AuthTokens("test-access-token", "test-refresh-token"));

      given(authService.register(any(RegisterRequest.class))).willReturn(authResponse);

      // When
      mockMvc
          .perform(
              post("/api/users/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      // Then
      ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
      verify(authService).register(captor.capture());

      RegisterRequest capturedRequest = captor.getValue();
      assertThat(capturedRequest.email()).isEqualTo("test@example.com");
      assertThat(capturedRequest.password()).isEqualTo("Password@123!");
      assertThat(capturedRequest.name()).isEqualTo("John Doe");
    }
  }

  @Nested
  @DisplayName("Login Tests")
  class LoginTests {

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void shouldLoginUserSuccessfully() throws Exception {
      // Given
      LoginRequest request = new LoginRequest("test@example.com", "password123");

      AuthResponse authResponse =
          new AuthResponse(
              new UserResponse(
                  UUID.randomUUID(),
                  "test@test.com",
                  "Test User",
                  Role.MEMBER,
                  true,
                  LocalDateTime.now(),
                  LocalDateTime.now(),
                  LocalDateTime.now()),
              new AuthTokens("access-token", "refresh-token"));

      given(authService.login(any(LoginRequest.class))).willReturn(authResponse);

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Login successfully"))
          .andExpect(jsonPath("$.data.tokens.accessToken").value("access-token"))
          .andExpect(jsonPath("$.data.tokens.refreshToken").value("refresh-token"))
          .andExpect(jsonPath("$.data.user.id").exists());

      verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when login request is invalid")
    void shouldReturnBadRequestWhenLoginRequestIsInvalid() throws Exception {
      // Given
      LoginRequest request = new LoginRequest(null, "password123");
      // Missing required fields

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Token Refresh Tests")
  class TokenRefreshTests {

    @Test
    @DisplayName("Should refresh tokens successfully")
    void shouldRefreshTokensSuccessfully() throws Exception {
      // Given
      RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

      AuthResponse authResponse =
          new AuthResponse(
              new UserResponse(
                  UUID.randomUUID(),
                  "test@email.com",
                  "Test User",
                  Role.MEMBER,
                  false,
                  LocalDateTime.now(),
                  LocalDateTime.now(),
                  LocalDateTime.now()),
              new AuthTokens("new-access-token", "new-refresh-token"));

      given(authService.refreshTokens(anyString())).willReturn(authResponse);

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Tokens refreshed successfully"))
          .andExpect(jsonPath("$.data.tokens.accessToken").value("new-access-token"))
          .andExpect(jsonPath("$.data.tokens.refreshToken").value("new-refresh-token"));

      verify(authService).refreshTokens("refresh-token");
    }

    @Test
    @DisplayName("Should return 400 when refresh token is missing")
    void shouldReturnBadRequestWhenRefreshTokenIsMissing() throws Exception {
      // Given
      RefreshTokenRequest request = new RefreshTokenRequest(null);
      // Missing refresh token

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Logout Tests")
  class LogoutTests {

    @Test
    @DisplayName("Should logout from all devices successfully")
    void shouldLogoutFromAllDevicesSuccessfully() throws Exception {
      // Given
      RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Logout successful"))
          .andExpect(jsonPath("$.data").doesNotExist());

      verify(authService).logout("refresh-token");
    }

    @Test
    @DisplayName("Should logout from single device successfully")
    void shouldLogoutFromSingleDeviceSuccessfully() throws Exception {
      // Given
      RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/logout-single")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Logout from current device successful"))
          .andExpect(jsonPath("$.data").doesNotExist());

      verify(authService).logoutSingle("refresh-token");
    }

    @Test
    @DisplayName("Should return 400 when logout request is invalid")
    void shouldReturnBadRequestWhenLogoutRequestIsInvalid() throws Exception {
      // Given
      RefreshTokenRequest request = new RefreshTokenRequest("");
      // Missing refresh token

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle service exceptions properly")
    void shouldHandleServiceExceptionsProperly() throws Exception {
      // Given
      LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

      given(authService.login(any(LoginRequest.class)))
          .willThrow(new RuntimeException("Invalid credentials"));

      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 400 for malformed JSON")
    void shouldReturnBadRequestForMalformedJson() throws Exception {
      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{invalid json"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 for unsupported media type")
    void shouldReturnUnsupportedMediaTypeForWrongContentType() throws Exception {
      // When & Then
      mockMvc
          .perform(
              post("/api/users/auth/login").contentType(MediaType.TEXT_PLAIN).content("some text"))
          .andExpect(status().isUnsupportedMediaType());
    }
  }
}
