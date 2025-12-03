package org.nkcoder.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.config.IntegrationTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@IntegrationTest
@DisplayName("Auth Flow Integration Tests")
class AuthFlowIntegrationTest {

  @LocalServerPort private int port;

  @BeforeEach
  void setupRestAssured() {
    RestAssured.port = port;
    RestAssured.basePath = "/api/users";
  }

  @Nested
  @DisplayName("Complete Authentication Flow")
  class CompleteAuthFlow {

    @Test
    @DisplayName("register → login → access protected → refresh → logout")
    void fullAuthenticationFlow() {
      // Step 1: Register
      Response registerResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  """
                  {
                    "email": "flow@example.com",
                    "password": "Password123",
                    "name": "Flow Test User",
                    "role": "MEMBER"
                  }
                  """)
              .when()
              .post("/auth/register")
              .then()
              .statusCode(anyOf(is(200), is(201)))
              .body("data.user.email", equalTo("flow@example.com"))
              .body("data.tokens.accessToken", notNullValue())
              .body("data.tokens.refreshToken", notNullValue())
              .extract()
              .response();

      String accessToken = registerResponse.jsonPath().getString("data.tokens.accessToken");
      String refreshToken = registerResponse.jsonPath().getString("data.tokens.refreshToken");

      // Step 2: Access protected endpoint
      given()
          .header("Authorization", "Bearer " + accessToken)
          .when()
          .get("/me")
          .then()
          .statusCode(200)
          .body("data.email", equalTo("flow@example.com"))
          .body("data.name", equalTo("Flow Test User"));

      // Step 3: Refresh tokens
      Response refreshResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  """
                  {
                    "refreshToken": "%s"
                  }
                  """
                      .formatted(refreshToken))
              .when()
              .post("/auth/refresh")
              .then()
              .statusCode(200)
              .body("data.tokens.accessToken", notNullValue())
              .body("data.tokens.refreshToken", notNullValue())
              .extract()
              .response();

      String newAccessToken = refreshResponse.jsonPath().getString("data.tokens.accessToken");
      String newRefreshToken = refreshResponse.jsonPath().getString("data.tokens.refreshToken");

      // Verify tokens rotated
      //      assertThat(newAccessToken).isNotEqualTo(accessToken);
      //      assertThat(newRefreshToken).isNotEqualTo(refreshToken);

      // Step 4: Old refresh token should be invalid
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "refreshToken": "%s"
              }
              """
                  .formatted(refreshToken))
          .when()
          .post("/auth/refresh")
          .then()
          .statusCode(401);

      // Step 5: New access token works
      given()
          .header("Authorization", "Bearer " + newAccessToken)
          .when()
          .get("/me")
          .then()
          .statusCode(200);

      // Step 6: Logout
      given()
          .header("Authorization", "Bearer " + newAccessToken)
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "refreshToken": "%s"
              }
              """
                  .formatted(newRefreshToken))
          .when()
          .post("/auth/logout")
          .then()
          .statusCode(200);

      // Step 7: Refresh token invalid after logout
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "refreshToken": "%s"
              }
              """
                  .formatted(newRefreshToken))
          .when()
          .post("/auth/refresh")
          .then()
          .statusCode(401);
    }
  }

  @Nested
  @DisplayName("Registration")
  class Registration {

    @Test
    @DisplayName("registers new user successfully")
    void registersNewUser() {
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "newuser@example.com",
                "password": "Password123",
                "name": "New User",
                "role": "MEMBER"
              }
              """)
          .when()
          .post("/auth/register")
          .then()
          .statusCode(anyOf(is(200), is(201)))
          .body("data.user.email", equalTo("newuser@example.com"))
          .body("data.user.name", equalTo("New User"))
          .body("data.user.role", equalTo("MEMBER"))
          .body("data.tokens.accessToken", notNullValue())
          .body("data.tokens.refreshToken", notNullValue());
    }

    @Test
    @DisplayName("rejects duplicate email")
    void rejectsDuplicateEmail() {
      // First registration
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "duplicate@example.com",
                "password": "Password123",
                "name": "First User",
                "role": "MEMBER"
              }
              """)
          .when()
          .post("/auth/register")
          .then()
          .statusCode(anyOf(is(200), is(201)));

      // Second registration with same email
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "duplicate@example.com",
                "password": "Password123",
                "name": "Second User",
                "role": "MEMBER"
              }
              """)
          .when()
          .post("/auth/register")
          .then()
          .statusCode(400)
          .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("normalizes email to lowercase")
    void normalizesEmail() {
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "UPPERCASE@EXAMPLE.COM",
                "password": "Password123",
                "name": "Uppercase Email",
                "role": "MEMBER"
              }
              """)
          .when()
          .post("/auth/register")
          .then()
          .statusCode(anyOf(is(200), is(201)))
          .body("data.user.email", equalTo("uppercase@example.com"));
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
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "login@example.com",
                "password": "Password1234"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(200)
          .body("data.user.email", equalTo("login@example.com"))
          .body("data.tokens.accessToken", notNullValue());
    }

    @Test
    @DisplayName("rejects wrong password")
    void rejectsWrongPassword() {
      registerUser("wrongpass@example.com", "Password123", "Wrong Pass User");

      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "wrongpass@example.com",
                "password": "WrongPassword123"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(401)
          .body("message", equalTo("Invalid email or password"));
    }

    @Test
    @DisplayName("rejects non-existent email")
    void rejectsNonExistentEmail() {
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "nonexistent@example.com",
                "password": "Password123"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(401)
          .body("message", equalTo("Invalid email or password"));
    }
  }

  @Nested
  @DisplayName("Profile Operations")
  class ProfileOperations {

    @Test
    @DisplayName("updates profile successfully")
    void updatesProfile() {
      String accessToken =
          registerAndGetToken("profile@example.com", "Password123", "Original Name");

      given()
          .header("Authorization", "Bearer " + accessToken)
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "name": "Updated Name"
              }
              """)
          .when()
          .patch("/me")
          .then()
          .statusCode(200)
          .body("data.name", equalTo("Updated Name"));
    }

    @Test
    @DisplayName("changes password successfully")
    void changesPassword() {
      String accessToken =
          registerAndGetToken("password@example.com", "OldPassword123", "Password User");

      // Change password
      given()
          .header("Authorization", "Bearer " + accessToken)
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "currentPassword": "OldPassword123",
                "newPassword": "NewPassword123",
                "confirmPassword": "NewPassword123"
              }
              """)
          .when()
          .patch("/me/password")
          .then()
          .statusCode(200);

      // Login with new password works
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "password@example.com",
                "password": "NewPassword123"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(200);

      // Login with old password fails
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                "email": "password@example.com",
                "password": "OldPassword123"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(401);
    }
  }

  @Nested
  @DisplayName("Token Security")
  class TokenSecurity {

    @Test
    @DisplayName("rejects expired/invalid access token")
    void rejectsInvalidToken() {
      given()
          .header("Authorization", "Bearer invalid.token.here")
          .when()
          .get("/me")
          .then()
          .statusCode(401);
    }

    @Test
    @DisplayName("rejects request without token")
    void rejectsNoToken() {
      given().when().get("/me").then().statusCode(401);
    }
  }

  // Helper methods
  private void registerUser(String email, String password, String name) {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "email": "%s",
              "password": "%s",
              "name": "%s",
              "role": "MEMBER"
            }
            """
                .formatted(email, password, name))
        .when()
        .post("/auth/register")
        .then()
        .statusCode(anyOf(is(200), is(201)));
  }

  private String registerAndGetToken(String email, String password, String name) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "email": "%s",
                  "password": "%s",
                  "name": "%s",
                  "role": "MEMBER"
                }
                """
                    .formatted(email, password, name))
            .when()
            .post("/auth/register")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract()
            .response();

    return response.jsonPath().getString("data.tokens.accessToken");
  }
}
