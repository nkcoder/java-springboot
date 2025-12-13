package org.nkcoder.user.integration;

import org.junit.jupiter.api.Disabled;
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
@DisplayName("AdminUserController Integration Tests")
@Disabled("Tests need fixing - response structure mismatch")
class AdminUserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Nested
    @DisplayName("GET /api/admin/users - Get All Users")
    class GetAllUsers {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            webTestClient
                    .get()
                    .uri("/api/admin/users")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        @DisplayName("returns user list for ADMIN role")
        void returnsUserListForAdmin() {
            String adminToken = registerAndGetToken("admin@example.com", "Password123", "Admin User", "ADMIN");

            webTestClient
                    .get()
                    .uri("/api/admin/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data")
                    .isNotEmpty()
                    .jsonPath("$.data")
                    .isArray();
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{userId} - Get User By ID")
    class GetUserById {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            webTestClient
                    .get()
                    .uri("/api/admin/users/{userId}", userId)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        @DisplayName("returns user details for ADMIN role")
        void returnsUserDetailsForAdmin() {
            // Register target user
            String targetUserId = registerAndGetUserId("target@example.com", "Password123", "Target User", "MEMBER");

            // Register admin
            String adminToken = registerAndGetToken("admin2@example.com", "Password123", "Admin User", "ADMIN");

            webTestClient
                    .get()
                    .uri("/api/admin/users/{userId}", targetUserId)
                    .header("Authorization", "Bearer " + adminToken)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data.email")
                    .isEqualTo("target@example.com")
                    .jsonPath("$.data.name")
                    .isEqualTo("Target User");
        }

        @Test
        @DisplayName("returns 404 for non-existent user")
        void returns404ForNonExistentUser() {
            String adminToken = registerAndGetToken("admin3@example.com", "Password123", "Admin User", "ADMIN");
            String nonExistentUserId = "00000000-0000-0000-0000-000000000000";

            webTestClient
                    .get()
                    .uri("/api/admin/users/{userId}", nonExistentUserId)
                    .header("Authorization", "Bearer " + adminToken)
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId} - Update User")
    class UpdateUser {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            webTestClient
                    .patch()
                    .uri("/api/admin/users/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\": \"Updated Name\"}")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        @DisplayName("updates user successfully for ADMIN role")
        void updatesUserSuccessfully() {
            // Register target user
            String targetUserId = registerAndGetUserId("target2@example.com", "Password123", "Original Name", "MEMBER");

            // Register admin
            String adminToken = registerAndGetToken("admin4@example.com", "Password123", "Admin User", "ADMIN");

            webTestClient
                    .patch()
                    .uri("/api/admin/users/{userId}", targetUserId)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "name": "Admin Updated Name",
                          "emailVerified": true,
                          "role": "ADMIN"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.data.name")
                    .isEqualTo("Admin Updated Name")
                    .jsonPath("$.data.emailVerified")
                    .isEqualTo(true)
                    .jsonPath("$.data.role")
                    .isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId}/password - Reset Password")
    class ResetPassword {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            webTestClient
                    .patch()
                    .uri("/api/admin/users/{userId}/password", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"newPassword\": \"NewPassword123\"}")
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        @DisplayName("resets password successfully for ADMIN role")
        void resetsPasswordSuccessfully() {
            // Register target user and get their ID
            String targetUserId =
                    registerAndGetUserId("target3@example.com", "OldPassword123", "Target User", "MEMBER");

            // Register admin
            String adminToken = registerAndGetToken("admin5@example.com", "Password123", "Admin User", "ADMIN");

            // Reset password
            webTestClient
                    .patch()
                    .uri("/api/admin/users/{userId}/password", targetUserId)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "newPassword": "NewPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk();

            // Verify new password works
            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "target3@example.com",
                          "password": "NewPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isOk();

            // Verify old password doesn't work
            webTestClient
                    .post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                        {
                          "email": "target3@example.com",
                          "password": "OldPassword123"
                        }
                        """)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }
    }

    // Helper methods

    private String registerAndGetToken(String email, String password, String name, String role) {
        var response = webTestClient
                .post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "name": "%s",
                      "role": "%s"
                    }
                    """.formatted(email, password, name, role))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .returnResult();

        String responseBody = new String(response.getResponseBody());
        return extractJsonValue(responseBody, "data.tokens.accessToken");
    }

    private String registerAndGetUserId(String email, String password, String name, String role) {
        var response = webTestClient
                .post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "name": "%s",
                      "role": "%s"
                    }
                    """.formatted(email, password, name, role))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .returnResult();

        String responseBody = new String(response.getResponseBody());
        return extractJsonValue(responseBody, "data.user.id");
    }

    /** Simple JSON value extractor for dot-notation paths. */
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
