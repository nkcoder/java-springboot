package org.nkcoder.user.application.service;

import java.util.UUID;
import org.nkcoder.shared.kernel.domain.event.DomainEventPublisher;
import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.nkcoder.shared.kernel.exception.ResourceNotFoundException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.nkcoder.user.application.dto.command.AdminResetPasswordCommand;
import org.nkcoder.user.application.dto.command.AdminUpdateUserCommand;
import org.nkcoder.user.application.dto.command.ChangePasswordCommand;
import org.nkcoder.user.application.dto.command.UpdateProfileCommand;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.application.port.AuthContextPort;
import org.nkcoder.user.domain.event.UserProfileUpdatedEvent;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for user command operations. */
@Service
@Transactional
public class UserCommandService {

    private static final Logger logger = LoggerFactory.getLogger(UserCommandService.class);

    private final UserRepository userRepository;
    private final AuthContextPort authContextPort;
    private final DomainEventPublisher eventPublisher;

    public UserCommandService(
            UserRepository userRepository, AuthContextPort authContextPort, DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.authContextPort = authContextPort;
        this.eventPublisher = eventPublisher;
    }

    /** Updates a user's profile. */
    public UserDto updateProfile(UpdateProfileCommand command) {
        logger.info("Updating profile for user: {}", command.userId());

        User user = findUserOrThrow(command.userId());

        UserProfileUpdatedEvent event = user.updateProfile(UserName.of(command.name()));

        User savedUser = userRepository.save(user);
        eventPublisher.publish(event);

        logger.info("Profile updated for user: {}", command.userId());
        return UserDto.from(savedUser);
    }

    /** Changes a user's password. */
    public void changePassword(ChangePasswordCommand command) {
        logger.info("Changing password for user: {}", command.userId());

        if (!userRepository.existsById(UserId.of(command.userId()))) {
            throw new ResourceNotFoundException("User not found: " + command.userId());
        }

        if (!authContextPort.verifyPassword(command.userId(), command.currentPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        authContextPort.changePassword(command.userId(), command.newPassword());

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

        User savedUser = userRepository.save(user);

        logger.info("Admin updated user: {}", command.targetUserId());
        return UserDto.from(savedUser);
    }

    /** Admin operation: Resets a user's password. */
    public void adminResetPassword(AdminResetPasswordCommand command) {
        logger.info("Admin resetting password for user: {}", command.targetUserId());

        if (!userRepository.existsById(UserId.of(command.targetUserId()))) {
            throw new ResourceNotFoundException("User not found: " + command.targetUserId());
        }

        authContextPort.changePassword(command.targetUserId(), command.newPassword());

        logger.info("Admin reset password for user: {}", command.targetUserId());
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository
                .findById(UserId.of(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
