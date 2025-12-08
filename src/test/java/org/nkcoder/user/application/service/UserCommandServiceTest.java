package org.nkcoder.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandService")
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthContextPort authContextPort;

    @Mock
    private DomainEventPublisher eventPublisher;

    private UserCommandService userCommandService;

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandService(userRepository, authContextPort, eventPublisher);
    }

    private User createTestUser(UUID userId, String email, String name) {
        return User.reconstitute(
                UserId.of(userId),
                Email.of(email),
                UserName.of(name),
                UserRole.MEMBER,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates profile successfully")
        void updatesProfileSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Old Name");
            UpdateProfileCommand command = new UpdateProfileCommand(userId, "New Name");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userCommandService.updateProfile(command);

            assertThat(result.name()).isEqualTo("New Name");
            verify(eventPublisher).publish(any(UserProfileUpdatedEvent.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            UpdateProfileCommand command = new UpdateProfileCommand(userId, "New Name");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.updateProfile(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("changes password successfully")
        void changesPasswordSuccessfully() {
            UUID userId = UUID.randomUUID();
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPass", "newPass");

            given(userRepository.existsById(any(UserId.class))).willReturn(true);
            given(authContextPort.verifyPassword(eq(userId), eq("oldPass"))).willReturn(true);

            userCommandService.changePassword(command);

            verify(authContextPort).changePassword(eq(userId), eq("newPass"));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPass", "newPass");

            given(userRepository.existsById(any(UserId.class))).willReturn(false);

            assertThatThrownBy(() -> userCommandService.changePassword(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("throws ValidationException when current password is incorrect")
        void throwsWhenCurrentPasswordIncorrect() {
            UUID userId = UUID.randomUUID();
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "wrongPass", "newPass");

            given(userRepository.existsById(any(UserId.class))).willReturn(true);
            given(authContextPort.verifyPassword(eq(userId), eq("wrongPass"))).willReturn(false);

            assertThatThrownBy(() -> userCommandService.changePassword(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Current password is incorrect");

            verify(authContextPort, never()).changePassword(any(), any());
        }
    }

    @Nested
    @DisplayName("adminUpdateUser")
    class AdminUpdateUser {

        @Test
        @DisplayName("updates user name successfully")
        void updatesUserNameSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Old Name");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, "New Name", null);

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userCommandService.adminUpdateUser(command);

            assertThat(result.name()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("updates user email successfully")
        void updatesUserEmailSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "old@example.com", "Test User");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, null, "new@example.com");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.existsByEmailExcludingId(any(Email.class), any(UserId.class)))
                    .willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userCommandService.adminUpdateUser(command);

            assertThat(result.email()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("throws ValidationException when email already in use")
        void throwsWhenEmailAlreadyInUse() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "old@example.com", "Test User");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, null, "taken@example.com");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.existsByEmailExcludingId(any(Email.class), any(UserId.class)))
                    .willReturn(true);

            assertThatThrownBy(() -> userCommandService.adminUpdateUser(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Email already in use");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, "Name", null);

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.adminUpdateUser(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("skips name update when name is blank")
        void skipsNameUpdateWhenBlank() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Original Name");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, "  ", null);

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userCommandService.adminUpdateUser(command);

            assertThat(result.name()).isEqualTo("Original Name");
        }

        @Test
        @DisplayName("skips email update when email is blank")
        void skipsEmailUpdateWhenBlank() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "original@example.com", "Test User");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, null, "  ");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userCommandService.adminUpdateUser(command);

            assertThat(result.email()).isEqualTo("original@example.com");
        }
    }

    @Nested
    @DisplayName("adminResetPassword")
    class AdminResetPassword {

        @Test
        @DisplayName("resets password successfully")
        void resetsPasswordSuccessfully() {
            UUID userId = UUID.randomUUID();
            AdminResetPasswordCommand command = new AdminResetPasswordCommand(userId, "newPassword");

            given(userRepository.existsById(any(UserId.class))).willReturn(true);

            userCommandService.adminResetPassword(command);

            verify(authContextPort).changePassword(eq(userId), eq("newPassword"));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            AdminResetPasswordCommand command = new AdminResetPasswordCommand(userId, "newPassword");

            given(userRepository.existsById(any(UserId.class))).willReturn(false);

            assertThatThrownBy(() -> userCommandService.adminResetPassword(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }
}
