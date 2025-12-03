package org.nkcoder.service;

import java.time.LocalDateTime;
import java.util.Optional;
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
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  public static final String USER_NOT_FOUND_ID = "User not found with id: ";
  public static final String USER_NOT_FOUND_EMAIL = "User not found with email";
  public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
  public static final String CURRENT_PASSWORD_INCORRECT = "Current password is incorrect";

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
    return userRepository
        .findById(id)
        .flatMap(userMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ID + id));
  }

  @Transactional(readOnly = true)
  public UserResponse findByEmail(String email) {
    logger.debug("Finding user by email: {}", email);
    return userRepository
        .findByEmail(email.toLowerCase())
        .flatMap(userMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_EMAIL + email));
  }

  @Transactional
  public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
    logger.debug("Updating profile for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ID + userId));

    Optional.ofNullable(request.email())
        .filter(StringUtils::hasText)
        .filter(email -> !email.equals(user.getEmail()))
        .ifPresent(
            email -> {
              if (userRepository.existsByEmail(email.toLowerCase())) {
                throw new ValidationException(EMAIL_ALREADY_EXISTS);
              }
              user.updateEmail(email);
            });

    Optional.ofNullable(request.name()).filter(StringUtils::hasText).ifPresent(user::updateName);

    User updatedUser = userRepository.save(user);
    logger.debug("Profile updated successfully for user: {}", userId);

    return userMapper.toResponseOrThrow(updatedUser);
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest request) {
    logger.debug("Changing password for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ID + userId));

    // Password confirmation if now validated by @PasswordMatch annotation

    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new ValidationException(CURRENT_PASSWORD_INCORRECT);
    }

    user.changePassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    logger.debug("Password changed successfully for user: {}", userId);
  }

  @Transactional
  public void updateLastLogin(UUID userId) {
    logger.debug("Updating last login for user: {}", userId);
    userRepository.updateLastLoginAt(userId, LocalDateTime.now());
  }

  @Transactional
  public void changeUserPassword(UUID userId, String newPassword) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ID + userId));

    // Update password
    user.changePassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }
}
