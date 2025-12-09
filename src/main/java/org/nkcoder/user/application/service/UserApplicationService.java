package org.nkcoder.user.application.service;

import java.util.List;
import java.util.UUID;
import org.nkcoder.shared.kernel.domain.event.DomainEventPublisher;
import org.nkcoder.shared.kernel.exception.ResourceNotFoundException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.nkcoder.user.application.dto.command.AdminResetPasswordCommand;
import org.nkcoder.user.application.dto.command.AdminUpdateUserCommand;
import org.nkcoder.user.application.dto.command.ChangePasswordCommand;
import org.nkcoder.user.application.dto.command.UpdateProfileCommand;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.repository.UserRepository;
import org.nkcoder.user.domain.service.AuthenticationService;
import org.nkcoder.user.domain.service.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for user operations (both commands and queries). */
@Service
@Transactional
public class UserApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(UserApplicationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final DomainEventPublisher eventPublisher;

    public UserApplicationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationService authenticationService,
            DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
        this.eventPublisher = eventPublisher;
    }

    // Query operations

    /** Gets a user by their ID. */
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        logger.debug("Getting user by ID: {}", userId);

        User user = findUserOrThrow(userId);
        return UserDto.from(user);
    }

    /** Gets all users (admin operation). */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        logger.debug("Getting all users");

        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    /** Checks if a user exists. */
    @Transactional(readOnly = true)
    public boolean userExists(UUID userId) {
        return userRepository.existsById(UserId.of(userId));
    }

    // Command operations

    /** Updates a user's profile. */
    public UserDto updateProfile(UpdateProfileCommand command) {
        logger.info("Updating profile for user: {}", command.userId());

        User user = findUserOrThrow(command.userId());

        user.updateProfile(UserName.of(command.name()));

        User savedUser = saveAndPublishEvents(user);

        logger.info("Profile updated for user: {}", command.userId());
        return UserDto.from(savedUser);
    }

    /** Changes a user's password. */
    public void changePassword(ChangePasswordCommand command) {
        logger.info("Changing password for user: {}", command.userId());

        User user = findUserOrThrow(command.userId());

        if (!authenticationService.verifyPassword(user, command.currentPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        logger.info("Password changed for user: {}", command.userId());
    }

    /** Admin operation: Updates a user's information. */
    public UserDto adminUpdateUser(AdminUpdateUserCommand command) {
        logger.info("Admin updating user: {}", command.targetUserId());

        User user = findUserOrThrow(command.targetUserId());

        if (command.name() != null && !command.name().isBlank()) {
            user.updateProfile(UserName.of(command.name()));
        }

        if (command.email() != null && !command.email().isBlank()) {
            Email newEmail = Email.of(command.email());

            if (userRepository.existsByEmailExcludingId(newEmail, user.getId())) {
                throw new ValidationException("Email already in use");
            }

            user.updateEmail(newEmail);
        }

        User savedUser = saveAndPublishEvents(user);

        logger.info("Admin updated user: {}", command.targetUserId());
        return UserDto.from(savedUser);
    }

    /** Admin operation: Resets a user's password. */
    public void adminResetPassword(AdminResetPasswordCommand command) {
        logger.info("Admin resetting password for user: {}", command.targetUserId());

        User user = findUserOrThrow(command.targetUserId());

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        logger.info("Admin reset password for user: {}", command.targetUserId());
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository
                .findById(UserId.of(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    /** Saves the user and publishes any domain events registered on the aggregate. */
    private User saveAndPublishEvents(User user) {
        User savedUser = userRepository.save(user);
        user.getDomainEvents().forEach(eventPublisher::publish);
        user.clearDomainEvents();
        return savedUser;
    }
}
