package org.nkcoder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.nkcoder.dto.user.ChangePasswordRequest;
import org.nkcoder.dto.user.UpdateProfileRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.entity.User;
import org.nkcoder.entity.UserTestFactory;
import org.nkcoder.enums.Role;
import org.nkcoder.exception.ResourceNotFoundException;
import org.nkcoder.exception.ValidationException;
import org.nkcoder.mapper.UserMapper;
import org.nkcoder.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private UserMapper userMapper;

  @InjectMocks private UserService userService;

  private final UUID userId = UUID.randomUUID();
  private final String email = "user@example.com";
  private final String name = "User Name";
  private final String encodedPassword = "encodedPassword";
  private final Role role = Role.MEMBER;
  private final LocalDateTime now = LocalDateTime.now();

  AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  void findById_success() {
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
    UserResponse userResponse = new UserResponse(userId, email, name, role, false, now, now, now);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userMapper.toResponse(user)).thenReturn(userResponse);

    UserResponse result = userService.findById(userId);

    assertEquals(userResponse, result);
    verify(userRepository).findById(userId);
    verify(userMapper).toResponse(user);
  }

  @Test
  void findById_notFound_throws() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
  }

  @Test
  void findByEmail_success() {
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
    UserResponse userResponse = new UserResponse(userId, email, name, role, false, now, now, now);

    when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(user));
    when(userMapper.toResponse(user)).thenReturn(userResponse);

    UserResponse result = userService.findByEmail(email);

    assertEquals(userResponse, result);
    verify(userRepository).findByEmail(email.toLowerCase());
    verify(userMapper).toResponse(user);
  }

  @Test
  void findByEmail_notFound_throws() {
    when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail(email));
  }

  @Test
  void updateProfile_success_updateNameAndEmail() {
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
    UpdateProfileRequest request = new UpdateProfileRequest("new@example.com", "New Name");
    User updatedUser =
        UserTestFactory.createWithId(
            userId, "new@example.com", encodedPassword, "New Name", role, false);
    UserResponse userResponse =
        new UserResponse(userId, "new@example.com", "New Name", role, false, now, now, now);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);
    when(userMapper.toResponse(updatedUser)).thenReturn(userResponse);

    UserResponse result = userService.updateProfile(userId, request);

    assertEquals(userResponse, result);
    verify(userRepository).findById(userId);
    verify(userRepository).existsByEmail("new@example.com");
    verify(userRepository).save(any(User.class));
    verify(userMapper).toResponse(updatedUser);
  }

  @Test
  void updateProfile_emailExists_throws() {
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
    UpdateProfileRequest request = new UpdateProfileRequest("existing@example.com", "New Name");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(ValidationException.class, () -> userService.updateProfile(userId, request));
  }

  @Test
  void updateProfile_updateNameOnly() {
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
    UpdateProfileRequest request = new UpdateProfileRequest(null, "Updated Name");
    User updatedUser =
        UserTestFactory.createWithId(userId, email, encodedPassword, "Updated Name", role, false);
    UserResponse userResponse =
        new UserResponse(userId, email, "Updated Name", role, false, now, now, now);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);
    when(userMapper.toResponse(updatedUser)).thenReturn(userResponse);

    UserResponse result = userService.updateProfile(userId, request);

    assertEquals(userResponse, result);
    verify(userRepository).save(any(User.class));
    verify(userMapper).toResponse(updatedUser);
  }

  @Test
  void updateProfile_userNotFound_throws() {
    UpdateProfileRequest request = new UpdateProfileRequest("new@example.com", "New Name");
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(userId, request));
  }

  @Test
  void changePassword_success() {
    ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass", "newPass");
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("oldPass", encodedPassword)).thenReturn(true);
    when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

    userService.changePassword(userId, request);

    verify(userRepository).save(argThat(u -> u.getPassword().equals("encodedNewPass")));
  }

  @Test
  void changePassword_passwordsDoNotMatch_throws() {
    ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass", "different");
    assertThrows(ValidationException.class, () -> userService.changePassword(userId, request));
  }

  @Test
  void changePassword_userNotFound_throws() {
    ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass", "newPass");
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> userService.changePassword(userId, request));
  }

  @Test
  void changePassword_currentPasswordIncorrect_throws() {
    ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass", "newPass");
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("oldPass", encodedPassword)).thenReturn(false);

    assertThrows(ValidationException.class, () -> userService.changePassword(userId, request));
  }

  @Test
  void updateLastLogin_success() {
    userService.updateLastLogin(userId);
    verify(userRepository).updateLastLoginAt(eq(userId), any(LocalDateTime.class));
  }

  @Test
  void changeUserPassword_success() {
    User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("adminNewPass")).thenReturn("encodedAdminPass");

    userService.changeUserPassword(userId, "adminNewPass");

    verify(userRepository).save(argThat(u -> u.getPassword().equals("encodedAdminPass")));
  }

  @Test
  void changeUserPassword_userNotFound_throws() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> userService.changeUserPassword(userId, "adminNewPass"));
  }
}
