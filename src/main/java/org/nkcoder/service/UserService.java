package org.nkcoder.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.nkcoder.dto.user.ChangePasswordRequest;
import org.nkcoder.dto.user.UpdateProfileRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.entity.User;
import org.nkcoder.exception.ResourceNotFoundException;
import org.nkcoder.exception.ValidationException;
import org.nkcoder.mapper.UserMapper;
import org.nkcoder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Autowired
  public UserService(
      UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  @Transactional(readOnly = true)
  public UserResponse findById(UUID id) {
    logger.debug("Finding user by ID: {}", id);
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    return userMapper.toResponse(user);
  }

  @Transactional(readOnly = true)
  public UserResponse findByEmail(String email) {
    logger.debug("Finding user by email: {}", email);
    User user =
        userRepository
            .findByEmail(email.toLowerCase())
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));
    return userMapper.toResponse(user);
  }

  public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
    logger.debug("Updating profile for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    // Validate email uniqueness if email is being updated
    if (StringUtils.hasText(request.email()) && !request.email().equals(user.getEmail())) {
      if (userRepository.existsByEmail(request.email().toLowerCase())) {
        throw new ValidationException("Email already exists");
      }
      user.updateEmail(request.email());
    }

    // Update name if provided
    if (StringUtils.hasText(request.name())) {
      user.updateName(request.name());
    }

    User updatedUser = userRepository.save(user);
    logger.debug("Profile updated successfully for user: {}", userId);

    return userMapper.toResponse(updatedUser);
  }

  public void changePassword(UUID userId, ChangePasswordRequest request) {
    logger.debug("Changing password for user: {}", userId);

    // Validate password confirmation
    if (!request.newPassword().equals(request.confirmPassword())) {
      throw new ValidationException("New password and confirmation password do not match");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    // Validate current password
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new ValidationException("Current password is incorrect");
    }

    // Update password
    user.changePassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    logger.debug("Password changed successfully for user: {}", userId);
  }

  public void updateLastLogin(UUID userId) {
    logger.debug("Updating last login for user: {}", userId);
    userRepository.updateLastLoginAt(userId, LocalDateTime.now());
  }

  // Admin can change any user's password
  public void changeUserPassword(UUID userId, String newPassword) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    // Update password
    user.changePassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }
}
