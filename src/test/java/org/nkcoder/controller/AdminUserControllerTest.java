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
import org.nkcoder.dto.user.ChangePasswordRequest;
import org.nkcoder.dto.user.UpdateProfileRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.enums.Role;
import org.nkcoder.security.JwtAuthenticationFilter;
import org.nkcoder.service.UserService;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("AdminUserController tests")
@WebMvcTest(
    controllers = AdminUserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {JwtAuthenticationFilter.class})
    })
class AdminUserControllerTest extends BaseControllerTest {

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
          .perform(get("/api/admin/users/{userId}", testUserId))
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
              patch("/api/admin/users/{userId}", testUserId)
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
              "OldPassword123!",
              "NewAdminPassword123!",
              "NewAdminPassword123!"); // Only newPassword is used for admin
      doNothing().when(userService).changeUserPassword(testUserId, "NewAdminPassword123!");

      // When & Then
      mockMvc
          .perform(
              patch("/api/admin/users/{userId}/password", testUserId)
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
              patch("/api/admin/users/{userId}", testUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }
}
