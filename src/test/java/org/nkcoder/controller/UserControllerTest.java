package org.nkcoder.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.config.SecurityConfig;
import org.nkcoder.dto.user.ChangePasswordRequest;
import org.nkcoder.dto.user.UpdateProfileRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.enums.Role;
import org.nkcoder.security.JwtAuthenticationEntryPoint;
import org.nkcoder.security.JwtAuthenticationFilter;
import org.nkcoder.service.UserService;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("UserController tests")
@WebMvcTest(
    value = UserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {
            JwtAuthenticationFilter.class,
            SecurityConfig.class,
            JwtAuthenticationEntryPoint.class
          })
    })
class UserControllerTest extends BaseControllerTest {

  @MockitoBean private UserService userService;

  private final UUID testUserId = UUID.randomUUID();
  private final String testEmail = "test@example.com";

  private UserResponse createTestUserResponse(UUID userId, String email, String name) {
    return new UserResponse(
        userId,
        email,
        name,
        Role.MEMBER,
        true,
        LocalDateTime.now(),
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  @Nested
  @DisplayName("User Profile Tests")
  @WithMockUser
  class UserProfileTests {

    @Test
    @DisplayName("Should get user profile successfully")
    void shouldGetUserProfileSuccessfully() throws Exception {
      // Given
      UserResponse userResponse = createTestUserResponse(testUserId, testEmail, "John Doe");
      given(userService.findById(testUserId)).willReturn(userResponse);

      // When & Then
      mockMvc
          .perform(
              get("/api/users/me")
                  .requestAttr("userId", testUserId)
                  .requestAttr("email", testEmail))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("User profile retrieved successfully"))
          .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
          .andExpect(jsonPath("$.data.email").value(testEmail))
          .andExpect(jsonPath("$.data.name").value("John Doe"));

      verify(userService).findById(testUserId);
    }

    @Test
    @DisplayName("Should update user profile successfully")
    //    @WithMockUser
    void shouldUpdateUserProfileSuccessfully() throws Exception {
      // Given
      UpdateProfileRequest request = new UpdateProfileRequest("newemail@example.com", "Jane Doe");
      UserResponse updatedResponse =
          createTestUserResponse(testUserId, "newemail@example.com", "Jane Doe");

      given(userService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
          .willReturn(updatedResponse);

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/me")
                  .requestAttr("userId", testUserId)
                  .requestAttr("email", testEmail)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Profile updated successfully"))
          .andExpect(jsonPath("$.data.email").value("newemail@example.com"))
          .andExpect(jsonPath("$.data.name").value("Jane Doe"));

      verify(userService).updateProfile(eq(testUserId), any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when update profile request is invalid")
    //    @WithMockUser
    void shouldReturnBadRequestWhenUpdateProfileRequestIsInvalid() throws Exception {
      // Given
      UpdateProfileRequest request =
          new UpdateProfileRequest("invalid-email", "A"); // Invalid email and name too short

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/me")
                  .requestAttr("userId", testUserId)
                  .requestAttr("email", testEmail)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should change password successfully")
    @WithMockUser
    void shouldChangePasswordSuccessfully() throws Exception {
      // Given
      ChangePasswordRequest request =
          new ChangePasswordRequest("OldPassword123!", "NewPassword123!", "NewPassword123!");
      doNothing()
          .when(userService)
          .changePassword(eq(testUserId), any(ChangePasswordRequest.class));

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/me/password")
                  .requestAttr("userId", testUserId)
                  .requestAttr("email", testEmail)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Password changed successfully"));

      verify(userService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when change password request is invalid")
    void shouldReturnBadRequestWhenChangePasswordRequestIsInvalid() throws Exception {
      // Given
      ChangePasswordRequest request =
          new ChangePasswordRequest("", "weak", "weak"); // Invalid password

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/me/password")
                  .requestAttr("userId", testUserId)
                  .requestAttr("email", testEmail)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Admin User Management Tests")
  @WithMockUser(roles = "ADMIN")
  class AdminUserManagementTests {

    @Test
    @DisplayName("Should get user by ID as admin")
    void shouldGetUserByIdAsAdmin() throws Exception {
      // Given
      UserResponse userResponse = createTestUserResponse(testUserId, testEmail, "Admin User");
      given(userService.findById(testUserId)).willReturn(userResponse);

      // When & Then
      mockMvc
          .perform(get("/api/users/{userId}", testUserId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("User retrieved successfully"))
          .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
          .andExpect(jsonPath("$.data.email").value(testEmail));

      verify(userService).findById(testUserId);
    }

    @Test
    @DisplayName("Should update user as admin")
    void shouldUpdateUserAsAdmin() throws Exception {
      // Given
      UpdateProfileRequest request = new UpdateProfileRequest("admin@example.com", "Admin Updated");
      UserResponse updatedResponse =
          createTestUserResponse(testUserId, "admin@example.com", "Admin Updated");

      given(userService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class)))
          .willReturn(updatedResponse);

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/{userId}", testUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("User updated successfully"))
          .andExpect(jsonPath("$.data.email").value("admin@example.com"))
          .andExpect(jsonPath("$.data.name").value("Admin Updated"));

      verify(userService).updateProfile(eq(testUserId), any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("Should change user password as admin")
    void shouldChangeUserPasswordAsAdmin() throws Exception {
      // Given
      ChangePasswordRequest request =
          new ChangePasswordRequest(
              "", "NewAdminPassword123!", ""); // Only newPassword is used for admin
      doNothing().when(userService).changeUserPassword(testUserId, "NewAdminPassword123!");

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/{userId}/password", testUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Password changed successfully"));

      verify(userService).changeUserPassword(testUserId, "NewAdminPassword123!");
    }

    @Test
    @DisplayName("Should return 400 when admin update request is invalid")
    void shouldReturnBadRequestWhenAdminUpdateRequestIsInvalid() throws Exception {
      // Given
      UpdateProfileRequest request =
          new UpdateProfileRequest("invalid-email", ""); // Invalid email and empty name

      // When & Then
      mockMvc
          .perform(
              patch("/api/users/{userId}", testUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }
}
