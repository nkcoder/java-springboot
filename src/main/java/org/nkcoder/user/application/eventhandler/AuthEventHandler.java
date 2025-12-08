package org.nkcoder.user.application.eventhandler;

import org.nkcoder.auth.domain.event.UserLoggedInEvent;
import org.nkcoder.auth.domain.event.UserRegisteredEvent;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for events from the Auth bounded context. Creates/updates User records in response
 * to Auth domain events.
 */
@Component
public class AuthEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(AuthEventHandler.class);

  private final UserRepository userRepository;

  public AuthEventHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Handles user registration events from Auth context. Creates a corresponding User record in the
   * User bounded context. Uses AFTER_COMMIT to run in a new transaction after Auth context commits,
   * avoiding entity conflicts when both contexts map the same table.
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleUserRegistered(UserRegisteredEvent event) {
    logger.info("Handling user registered event for user: {}", event.userId().value());

    UserId userId = UserId.of(event.userId().value());

    if (userRepository.existsById(userId)) {
      logger.warn("User already exists, skipping creation: {}", userId);
      return;
    }

    UserRole role = mapRole(event.role());
    User user = User.create(userId, event.email(), UserName.of(event.name()), role);

    userRepository.save(user);
    logger.info("User created in User context: {}", userId);
  }

  /**
   * Handles user login events from Auth context. Updates the last login timestamp in the User
   * bounded context. Uses AFTER_COMMIT to run after Auth context commits.
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleUserLoggedIn(UserLoggedInEvent event) {
    logger.debug("Handling user logged in event for user: {}", event.userId().value());

    UserId userId = UserId.of(event.userId().value());

    userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              user.recordLogin();
              userRepository.save(user);
              logger.debug("Updated last login for user: {}", userId);
            });
  }

  private UserRole mapRole(org.nkcoder.auth.domain.model.AuthRole authRole) {
    return switch (authRole) {
      case ADMIN -> UserRole.ADMIN;
      case MEMBER -> UserRole.MEMBER;
    };
  }
}
