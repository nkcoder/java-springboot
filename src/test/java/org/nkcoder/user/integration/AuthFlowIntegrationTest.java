package org.nkcoder.user.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.infrastructure.config.TestContainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Auth Flow Integration Tests")
class AuthFlowIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Nested
    @DisplayName("Complete Authentication Flow")
    class CompleteAuthFlow {

        @Test
        @DisplayName("register → login → access protected → refresh → logout")
        void fullAuthenticationFlow() {
            // Step 1: Register
            var registerResponse = webTestClient
                    .post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "flow@example.com",
                          "password": "Password123",
                          "name": "Flow Test User",
                          "role": "MEMBER"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.data.user.email")
                    .isEqualTo("flow@example.com")
                    .jsonPath("$.data.tokens.accessToken")
                    .isNotEmpty()
                    .jsonPath("$.data.tokens.refreshToken")
                    .isNotEmpty()
                    .returnResult();

            String responseBody = new String(registerResponse.getResponseBody());
            String accessToken = extractJsonValue(responseBody, "data.tokens.accessToken");
            String refreshToken = extractJsonValue(responseBody, "data.tokens.refreshToken");

            // Step 2: Access protected endpoint
            webTestClient
                    .get()
                    .uri("/api/users/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data.email")
                    .isEqualTo("flow@example.com")
                    .jsonPath("$.data.name")
                    .isEqualTo("Flow Test User");

            // Step 3: Refresh tokens
            var refreshResponse = webTestClient
                    .post()
                    .uri("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data.tokens.accessToken")
                    .isNotEmpty()
                    .jsonPath("$.data.tokens.refreshToken")
                    .isNotEmpty()
                    .returnResult();

            String refreshResponseBody = new String(refreshResponse.getResponseBody());
            String newAccessToken = extractJsonValue(refreshResponseBody, "data.tokens.accessToken");
            String newRefreshToken = extractJsonValue(refreshResponseBody, "data.tokens.refreshToken");

            // Step 4: Old refresh token should be invalid
            webTestClient
                    .post()
                    .uri("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();

            // Step 5: New access token works
            webTestClient
                    .get()
                    .uri("/api/users/me")
                    .header("Authorization", "Bearer " + newAccessToken)
                    .exchange()
                    .expectStatus()
                    .isOk();

            // Step 6: Logout
            webTestClient
                    .post()
                    .uri("/api/auth/logout")
                    .header("Authorization", "Bearer " + newAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(newRefreshToken))
                    .exchange()
                    .expectStatus()
                    .isOk();

            // Step 7: Refresh token invalid after logout
            webTestClient
                    .post()
                    .uri("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(newRefreshToken))
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("registers new user successfully")
        void registersNewUser() {
            webTestClient
                    .post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "newuser@example.com",
                          "password": "Password123",
                          "name": "New User",
                          "role": "MEMBER"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.data.user.email")
                    .isEqualTo("newuser@example.com")
                    .jsonPath("$.data.tokens.accessToken")
                    .isNotEmpty()
                    .jsonPath("$.data.tokens.refreshToken")
                    .isNotEmpty();
        }

        @Test
        @DisplayName("rejects duplicate email")
        void rejectsDuplicateEmail() {
            // First registration
            webTestClient
                    .post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "duplicate@example.com",
                          "password": "Password123",
                          "name": "First User",
                          "role": "MEMBER"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful();

            // Second registration with same email
            webTestClient
                    .post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "duplicate@example.com",
                          "password": "Password123",
                          "name": "Second User",
                          "role": "MEMBER"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo("User already exists");
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void normalizesEmail() {
            webTestClient
                    .post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "UPPERCASE@EXAMPLE.COM",
                          "password": "Password123",
                          "name": "Uppercase Email",
                          "role": "MEMBER"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.data.user.email")
                    .isEqualTo("uppercase@example.com");
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("logs in with valid credentials")
        void logsInSuccessfully() {
            // Register first
            registerUser("login@example.com", "Password1234", "Login User");

            // Login
            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "login@example.com",
                          "password": "Password1234"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data.user.email")
                    .isEqualTo("login@example.com")
                    .jsonPath("$.data.tokens.accessToken")
                    .isNotEmpty();
        }

        @Test
        @DisplayName("rejects wrong password")
        void rejectsWrongPassword() {
            registerUser("wrongpass@example.com", "Password123", "Wrong Pass User");

            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "wrongpass@example.com",
                          "password": "WrongPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo("Invalid email or password");
        }

        @Test
        @DisplayName("rejects non-existent email")
        void rejectsNonExistentEmail() {
            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "nonexistent@example.com",
                          "password": "Password123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo("Invalid email or password");
        }
    }

    @Nested
    @DisplayName("Profile Operations")
    class ProfileOperations {

        @Test
        @DisplayName("updates profile successfully")
        void updatesProfile() {
            String accessToken = registerAndGetToken("profile@example.com", "Password123", "Original Name");

            webTestClient
                    .patch()
                    .uri("/api/users/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "name": "Updated Name"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data.name")
                    .isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("changes password successfully")
        void changesPassword() {
            String accessToken = registerAndGetToken("password@example.com", "OldPassword123", "Password User");

            // Change password
            webTestClient
                    .patch()
                    .uri("/api/users/me/password")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "currentPassword": "OldPassword123",
                          "newPassword": "NewPassword123",
                          "confirmPassword": "NewPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk();

            // Login with new password works
            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "password@example.com",
                          "password": "NewPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk();

            // Login with old password fails
            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "password@example.com",
                          "password": "OldPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    @Nested
    @DisplayName("Token Security")
    class TokenSecurity {

        @Test
        @DisplayName("rejects expired/invalid access token")
        void rejectsInvalidToken() {
            webTestClient
                    .get()
                    .uri("/api/users/me")
                    .header("Authorization", "Bearer invalid.token.here")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        @DisplayName("rejects request without token")
        void rejectsNoToken() {
            webTestClient.get().uri("/api/users/me").exchange().expectStatus().isUnauthorized();
        }
    }

    // Helper methods

    private void registerUser(String email, String password, String name) {
        webTestClient
                .post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "name": "%s",
                      "role": "MEMBER"
                    }
                    """.formatted(email, password, name))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    private String registerAndGetToken(String email, String password, String name) {
        var response = webTestClient
                .post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "name": "%s",
                      "role": "MEMBER"
                    }
                    """.formatted(email, password, name))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .returnResult();

        String responseBody = new String(response.getResponseBody());
        return extractJsonValue(responseBody, "data.tokens.accessToken");
    }

    /** Simple JSON value extractor for dot-notation paths. Works for simple cases like "data.tokens.accessToken". */
    private String extractJsonValue(String json, String path) {
        String[] parts = path.split("\\.");
        String current = json;

        for (String part : parts) {
            int keyIndex = current.indexOf("\"" + part + "\"");
            if (keyIndex == -1) {
                return null;
            }
            current = current.substring(keyIndex + part.length() + 2);
            int colonIndex = current.indexOf(":");
            current = current.substring(colonIndex + 1).trim();

            if (current.startsWith("\"")) {
                // String value
                int endQuote = current.indexOf("\"", 1);
                if (endQuote == -1) {
                    return null;
                }
                if (parts[parts.length - 1].equals(part)) {
                    return current.substring(1, endQuote);
                }
            } else if (current.startsWith("{")) {
                // Object - continue to next part
                continue;
            }
        }
        return null;
    }
}
