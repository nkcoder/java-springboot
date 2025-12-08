package org.nkcoder.user.application.service;

import java.util.List;
import java.util.UUID;
import org.nkcoder.shared.kernel.exception.ResourceNotFoundException;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for user query operations. */
@Service
@Transactional(readOnly = true)
public class UserQueryService {

  private static final Logger logger = LoggerFactory.getLogger(UserQueryService.class);

  private final UserRepository userRepository;

  public UserQueryService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /** Gets a user by their ID. */
  public UserDto getUserById(UUID userId) {
    logger.debug("Getting user by ID: {}", userId);

    User user =
        userRepository
            .findById(UserId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    return UserDto.from(user);
  }

  /** Gets all users (admin operation). */
  public List<UserDto> getAllUsers() {
    logger.debug("Getting all users");

    return userRepository.findAll().stream().map(UserDto::from).toList();
  }

  /** Checks if a user exists. */
  public boolean userExists(UUID userId) {
    return userRepository.existsById(UserId.of(userId));
  }
}
