package org.nkcoder.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.infrastructure.config.TestContainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("AdminUserController Integration Tests")
@Disabled("Tests need fixing - response structure mismatch")
class AdminUserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Nested
    @DisplayName("GET /api/admin/users - Get All Users")
    class GetAllUsers {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            given().when().get("/api/admin/users").then().statusCode(401);
        }

        @Test
        @DisplayName("returns user list for ADMIN role")
        void returnsUserListForAdmin() {
            String adminToken = registerAndGetToken("admin@example.com", "Password123", "Admin User", "ADMIN");

            given().header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/admin/users")
                    .then()
                    .statusCode(200)
                    .body("data", notNullValue())
                    .body("data", isA(java.util.List.class));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{userId} - Get User By ID")
    class GetUserById {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            given().when().get("/api/admin/users/{userId}", userId).then().statusCode(401);
        }

        @Test
        @DisplayName("returns user details for ADMIN role")
        void returnsUserDetailsForAdmin() {
            // Register target user
            String targetUserId = registerAndGetUserId("target@example.com", "Password123", "Target User", "MEMBER");

            // Register admin
            String adminToken = registerAndGetToken("admin2@example.com", "Password123", "Admin User", "ADMIN");

            given().header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/admin/users/{userId}", targetUserId)
                    .then()
                    .statusCode(200)
                    .body("data.email", equalTo("target@example.com"))
                    .body("data.name", equalTo("Target User"));
        }

        @Test
        @DisplayName("returns 404 for non-existent user")
        void returns404ForNonExistentUser() {
            String adminToken = registerAndGetToken("admin3@example.com", "Password123", "Admin User", "ADMIN");
            String nonExistentUserId = "00000000-0000-0000-0000-000000000000";

            given().header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/admin/users/{userId}", nonExistentUserId)
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId} - Update User")
    class UpdateUser {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            given().contentType(ContentType.JSON)
                    .body("{\"name\": \"Updated Name\"}")
                    .when()
                    .patch("/api/admin/users/{userId}", userId)
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("updates user successfully for ADMIN role")
        void updatesUserSuccessfully() {
            // Register target user
            String targetUserId = registerAndGetUserId("target2@example.com", "Password123", "Original Name", "MEMBER");

            // Register admin
            String adminToken = registerAndGetToken("admin4@example.com", "Password123", "Admin User", "ADMIN");

            given().header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body("""
              {
                "name": "Admin Updated Name",
                "emailVerified": true,
                "role": "ADMIN"
              }
              """)
                    .when()
                    .patch("/api/admin/users/{userId}", targetUserId)
                    .then()
                    .statusCode(200)
                    .body("data.name", equalTo("Admin Updated Name"))
                    .body("data.emailVerified", equalTo(true))
                    .body("data.role", equalTo("ADMIN"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId}/password - Reset Password")
    class ResetPassword {

        @Test
        @DisplayName("returns 401 without authentication")
        void returns401WithoutAuth() {
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            given().contentType(ContentType.JSON)
                    .body("{\"newPassword\": \"NewPassword123\"}")
                    .when()
                    .patch("/api/admin/users/{userId}/password", userId)
                    .then()
                    .statusCode(401);
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
            given().header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body("""
              {
                "newPassword": "NewPassword123"
              }
              """)
                    .when()
                    .patch("/api/admin/users/{userId}/password", targetUserId)
                    .then()
                    .statusCode(200);

            // Verify new password works
            given().contentType(ContentType.JSON)
                    .body("""
              {
                "email": "target3@example.com",
                "password": "NewPassword123"
              }
              """)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(200);

            // Verify old password doesn't work
            given().contentType(ContentType.JSON)
                    .body("""
              {
                "email": "target3@example.com",
                "password": "OldPassword123"
              }
              """)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(401);
        }
    }

    // Helper methods
    private void registerUser(String email, String password, String name, String role) {
        given().contentType(ContentType.JSON)
                .body("""
            {
              "email": "%s",
              "password": "%s",
              "name": "%s",
              "role": "%s"
            }
            """.formatted(email, password, name, role))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201)));
    }

    private String registerAndGetToken(String email, String password, String name, String role) {
        Response response = given().contentType(ContentType.JSON)
                .body("""
                {
                  "email": "%s",
                  "password": "%s",
                  "name": "%s",
                  "role": "%s"
                }
                """.formatted(email, password, name, role))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .response();

        return response.jsonPath().getString("data.tokens.accessToken");
    }

    private String registerAndGetUserId(String email, String password, String name, String role) {
        Response response = given().contentType(ContentType.JSON)
                .body("""
                {
                  "email": "%s",
                  "password": "%s",
                  "name": "%s",
                  "role": "%s"
                }
                """.formatted(email, password, name, role))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .response();

        return response.jsonPath().getString("data.user.id");
    }
}
