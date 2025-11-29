package org.nkcoder.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.LoginRequest;
import org.nkcoder.dto.auth.RegisterRequest;
import org.nkcoder.dto.common.ApiResponse;
import org.nkcoder.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class AuthIntegrationTest extends IntegrationTestingBase {

  @Autowired private TestRestTemplate testRestTemplate;

  @Test
  @DisplayName("Should register, then login")
  public void shouldRegisterThenLogin() {
    // Register a new user
    String registerUrl = "/api/users/auth/register";
    ResponseEntity<ApiResponse<AuthResponse>> registerResponse =
        testRestTemplate.exchange(
            registerUrl,
            HttpMethod.POST,
            new HttpEntity<>(
                new RegisterRequest("user1@email.com", "Your@password123", "user1", Role.MEMBER)),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

    final var registerResponseBody = registerResponse.getBody();
    assertNotNull(registerResponseBody);
    assertEquals("User registered successfully", registerResponseBody.message());
    final var authResponse = registerResponseBody.data();
    assertNotNull(authResponse);
    assertNotNull(authResponse.tokens());
    assertNotNull(authResponse.tokens().accessToken());
    assertNotNull(authResponse.tokens().refreshToken());
    assertEquals("user1", authResponse.user().name());
    assertEquals("user1@email.com", authResponse.user().email());
    assertEquals(Role.MEMBER, authResponse.user().role());

    // Login with the registered user
    String loginUrl = "/api/users/auth/login";
    ResponseEntity<ApiResponse<AuthResponse>> loginResponse =
        testRestTemplate.exchange(
            loginUrl,
            HttpMethod.POST,
            new HttpEntity<>(new LoginRequest("user1@email.com", "Your@password123")),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

    final var loginResponseBody = loginResponse.getBody();
    assertNotNull(loginResponseBody);
    assertEquals("Login successfully", loginResponseBody.message());
    final var loginAuthResponse = loginResponseBody.data();
    assertNotNull(loginAuthResponse);
    assertNotNull(loginAuthResponse.tokens());
    assertNotNull(loginAuthResponse.tokens().accessToken());
    assertNotNull(loginAuthResponse.tokens().refreshToken());
    assertEquals("user1", loginAuthResponse.user().name());
    assertEquals("user1@email.com", loginAuthResponse.user().email());
    assertEquals(Role.MEMBER, loginAuthResponse.user().role());
  }
}
